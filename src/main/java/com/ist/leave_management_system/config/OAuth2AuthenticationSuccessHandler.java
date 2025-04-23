package com.ist.leave_management_system.config;

import com.ist.leave_management_system.model.Employee;
import com.ist.leave_management_system.model.UserRole;
import com.ist.leave_management_system.repository.EmployeeRepository;
import com.ist.leave_management_system.repository.UserRoleRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);
    
    private final JwtUtils jwtUtils;
    private final EmployeeRepository employeeRepository;
    private final UserRoleRepository userRoleRepository;

    public OAuth2AuthenticationSuccessHandler(
            JwtUtils jwtUtils,
            EmployeeRepository employeeRepository,
            UserRoleRepository userRoleRepository) {
        this.jwtUtils = jwtUtils;
        this.employeeRepository = employeeRepository;
        this.userRoleRepository = userRoleRepository;
        setDefaultTargetUrl("/api/auth/success");
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        logger.info("OAuth2 Authentication successful for principal: {}", authentication.getPrincipal());

        try {
            Employee employee;
            if (authentication.getPrincipal() instanceof OidcUser) {
                OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
                logger.info("Processing OIDC user with claims: {}", oidcUser.getClaims());
                employee = processOAuthPostLogin(oidcUser);
            } else if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                logger.info("Processing OAuth2 user with attributes: {}", oauth2User.getAttributes());
                 // Log each attribute for clarity
                oauth2User.getAttributes().forEach((key, value) -> 
                logger.info("OAuth2 attribute in success handler - {}: {}", key, value));
                employee = processOAuth2PostLogin(oauth2User);
            } else {
                throw new RuntimeException("Unsupported authentication type: " + authentication.getPrincipal().getClass());
            }

            String token;
            try {
                // Before generating token
                logger.info("Employee details: id={}, email={}, role={}", 
                    employee.getId(), 
                    employee.getEmail(), 
                    employee.getRole() != null ? employee.getRole().getRoleName() : "null");
                token = jwtUtils.generateToken(employee.getId(), employee.getRole().getRoleName());
            } catch (Exception e) {
                logger.error("Error generating token: ", e);
                throw e;
            }
            
            logger.info("Generated JWT token for user: {}", employee.getId());
            
            response.setHeader("Authorization", "Bearer " + token);
            String targetUrl = UriComponentsBuilder.fromUriString(getDefaultTargetUrl())
                .queryParam("token", token)
                .queryParam("id", employee.getId())
                .queryParam("role", employee.getRole().getRoleName())
                .build().toUriString();
            
            logger.info("Redirecting to: {}", targetUrl);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception e) {
            logger.error("Error processing OAuth2 authentication", e);
            String errorUrl = UriComponentsBuilder.fromUriString("/api/auth/login")
                .queryParam("error", "Authentication failed: " + e.getMessage())
                .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }

    private Employee processOAuthPostLogin(OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        if (email == null) {
            logger.error("Email not found in OIDC user claims");
            throw new RuntimeException("Email not found in OAuth2 user info");
        }
        
        logger.info("Looking up employee with email: {}", email);
        return employeeRepository.findByEmail(email)
                .orElseGet(() -> createNewEmployee(
                    email,
                    oidcUser.getGivenName(),
                    oidcUser.getFamilyName()
                ));
    }

    private Employee processOAuth2PostLogin(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        final String email = (String) attributes.get("mail") != null ? 
            (String) attributes.get("mail") : 
            (String) attributes.get("userPrincipalName");
        
        if (email == null) {
            logger.error("Email not found in OAuth2 user attributes");
            throw new RuntimeException("Email not found in OAuth2 user info");
        }

        String givenName = (String) attributes.get("givenName");
        String surname = (String) attributes.get("surname");
        
        logger.info("Looking up employee with email: {}", email);
        return employeeRepository.findByEmail(email)
                .orElseGet(() -> createNewEmployee(email, givenName, surname));
    }


    private Employee createNewEmployee(String email, String firstName, String lastName) {
        UserRole staffRole = userRoleRepository.findByRoleName("STAFF");
        if (staffRole == null) {
            logger.info("Creating new STAFF role");
            staffRole = userRoleRepository.save(
                UserRole.builder()
                    .roleName("STAFF")
                    .description("Regular staff access")
                    .build()
            );
        }

        Employee employee = Employee.builder()
                .email(email)
                .firstName(firstName != null ? firstName : "")
                .lastName(lastName != null ? lastName : "")
                .role(staffRole)
                .build();

        logger.info("Saving new employee: {}", employee.getEmail());
        return employeeRepository.save(employee);
    }
}