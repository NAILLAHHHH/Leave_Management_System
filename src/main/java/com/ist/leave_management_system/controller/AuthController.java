package com.ist.leave_management_system.controller;

import com.ist.leave_management_system.dto.LoginRequest;
import com.ist.leave_management_system.dto.LoginResponse;
import com.ist.leave_management_system.dto.RegisterRequest;
import com.ist.leave_management_system.model.Employee;
import com.ist.leave_management_system.service.AuthService;
import com.ist.leave_management_system.exception.UserAlreadyExistsException;
import jakarta.validation.Valid;
// import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.ist.leave_management_system.model.UserRole;
import com.ist.leave_management_system.repository.UserRoleRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;

    public AuthController(AuthService authService, PasswordEncoder passwordEncoder, UserRoleRepository userRoleRepository) {
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * Register a new employee
     *
     * @param request registration details
     * @return created Employee
     */
    @PostMapping("/register")
    public ResponseEntity<Employee> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            Employee employee = authService.registerUser(request);
            return ResponseEntity.ok(employee);
        } catch (UserAlreadyExistsException e) {
            throw new UserAlreadyExistsException("User already exists");
        }
    }

    /**
     * Authenticate user and return a JWT token
     *
     * @param request login credentials
     * @return LoginResponse with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.loginUser(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    /**
     * Get the current authenticated user's information
     * 
     * @return the authenticated Employee
     */
    @GetMapping("/me")
    public ResponseEntity<Employee> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Employee employee = authService.findByEmail(email);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<LoginResponse> handleOAuth2Success(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oauth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            
            // Check if user exists
            Employee employee = authService.findByEmail(email);
            if (employee == null) {
                // Create new user with default role
                UserRole defaultRole = userRoleRepository.findByRoleName("STAFF");
                if (defaultRole == null) {
                    throw new RuntimeException("Default role not found");
                }

                // Create new employee
                employee = Employee.builder()
                    .email(email)
                    .firstName(name.split(" ")[0])
                    .lastName(name.split(" ").length > 1 ? name.split(" ")[1] : "")
                    .password(passwordEncoder.encode("oauth2-generated-password"))
                    .role(defaultRole)
                    .build();

                employee = authService.saveEmployee(employee);
            }

            // Generate JWT token
            String token = authService.generateToken(employee);
            return ResponseEntity.ok(new LoginResponse(token, email, employee.getRole().getRoleName()));
        }
        
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/oauth2/test")
    public ResponseEntity<String> testOAuth2() {
        return ResponseEntity.ok("OAuth2 test endpoint is accessible");
    }
}
