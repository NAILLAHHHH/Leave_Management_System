package com.ist.leave_management_system.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints with all methods
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/login/**").permitAll()
                .requestMatchers("/error").permitAll()
                
                // Admin-only endpoints
                .requestMatchers(
                    "/api/leaves/*/approve",
                    "/api/leaves/*/reject",
                    "/api/leaves/pending-approval",
                    "/api/leaves/fix-balances"
                ).hasRole("ADMIN")
                
                // Employee endpoints
                .requestMatchers(
                    "/api/leaves",
                    "/api/leave-types/**",
                    "/api/leaves/my-leaves",
                    "/api/leaves/*/cancel",
                    "/api/leaves/status/*",
                    "/api/leaves/employee/*",
                    "/api/leaves/current"
                ).hasAnyRole("ADMIN", "STAFF")
                
                // All other requests need to be authenticated
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .defaultSuccessUrl("/api/auth/success")
                .failureUrl("/api/auth/login?error")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oauth2UserService())
                )
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/oauth2/authorization")
                )
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/login/oauth2/code/*")
                )
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    logger.error("OAUTH2 USER SERVICE CALLED - THIS SHOULD BE VISIBLE");
    
        return userRequest -> {
            try {
                logger.info("Starting OAuth2 user loading process");
                OAuth2User oAuth2User = delegate.loadUser(userRequest);
                logger.info("Successfully loaded OAuth2 user");
                
                // Force all attributes to be logged, even if they're null
                Map<String, Object> attributes = oAuth2User.getAttributes();
                for (String key : attributes.keySet()) {
                    logger.info("OAuth2 attribute: {} = {}", key, attributes.get(key));
                }
                
                // Use a try-catch specifically for the ID extraction
                String id = null;
                try {
                    id = (String) attributes.get("id");
                    logger.info("Found id attribute: {}", id);
                } catch (Exception e) {
                    logger.error("Error extracting id attribute", e);
                }
                
                if (id == null) {
                    logger.warn("ID is null, trying fallback attributes");
                    // Try other common identifiers
                    String[] possibleIds = {"sub", "oid", "userPrincipalName", "email"};
                    for (String possibleId : possibleIds) {
                        if (attributes.containsKey(possibleId)) {
                            logger.info("Found alternative ID attribute: {} = {}", possibleId, attributes.get(possibleId));
                            id = (String) attributes.get(possibleId);
                            attributes.put("id", id); // Add it as "id" for consistency
                            break;
                        }
                    }
                }
                
                if (id == null) {
                    logger.error("Could not find any ID attribute after trying all alternatives");
                    throw new OAuth2AuthenticationException("No suitable ID found in user attributes");
                }
                
                logger.info("Using ID: {}", id);
                
                return new DefaultOAuth2User(
                    Collections.emptyList(),
                    attributes,
                    "id"  // Use id as the name attribute key
                );
            } catch (Exception e) {
                logger.error("Error in OAuth2UserService", e);
                throw e;
            }
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        jwtAuthenticationConverter.setPrincipalClaimName("id");  // Use id as the principal claim name
        return jwtAuthenticationConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}