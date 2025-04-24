package com.ist.leave_management_system.config;

import com.ist.leave_management_system.dto.RegisterRequest;
import com.ist.leave_management_system.model.Employee;
import com.ist.leave_management_system.model.UserRole;
import com.ist.leave_management_system.repository.EmployeeRepository;
import com.ist.leave_management_system.repository.UserRoleRepository;
import com.ist.leave_management_system.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserRoleRepository userRoleRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) 
            throws IOException, ServletException {
            
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        String email = (String) attributes.get("email");
        if (email == null) {
            // For Microsoft Azure AD, sometimes email is in preferred_username
            email = (String) attributes.get("preferred_username");
        }
        
        Employee employee = employeeRepository.findByEmail(email).orElse(null);
        
        if (employee == null) {
            // Register new user
            String firstName = (String) attributes.get("given_name");
            String lastName = (String) attributes.get("family_name");
            
            // If name attributes are not available, extract from displayName or name
            if (firstName == null || lastName == null) {
                String name = (String) attributes.get("name");
                if (name != null) {
                    String[] nameParts = name.split(" ");
                    firstName = nameParts[0];
                    lastName = nameParts.length > 1 ? nameParts[1] : "";
                }
            }
            
            // Generate a random password for OAuth users
            String password = UUID.randomUUID().toString();
            
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setEmail(email);
            registerRequest.setFirstName(firstName != null ? firstName : "User");
            registerRequest.setLastName(lastName != null ? lastName : email.split("@")[0]);
            registerRequest.setPassword(password);
            
            // Default role for OAuth users
            UserRole defaultRole = userRoleRepository.findByRoleName("STAFF");
            registerRequest.setRoleName(defaultRole.getRoleName());
            
            employee = authService.registerUser(registerRequest);
        }
        
        // Generate JWT token
        String token = jwtUtils.generateToken(employee.getEmail());
        
        // Redirect to frontend with token
        String redirectUrl = frontendUrl + "/oauth2/redirect?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}