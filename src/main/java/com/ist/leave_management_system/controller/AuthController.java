package com.ist.leave_management_system.controller;

import com.ist.leave_management_system.dto.LoginRequest;
import com.ist.leave_management_system.dto.LoginResponse;
import com.ist.leave_management_system.dto.RegisterRequest;
import com.ist.leave_management_system.model.Employee;
import com.ist.leave_management_system.service.AuthService;
import com.ist.leave_management_system.exception.UserAlreadyExistsException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public AuthController(AuthService authService, OAuth2AuthorizedClientService authorizedClientService) {
        this.authService = authService;
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> getLoginOptions() {
        Map<String, String> options = new HashMap<>();
        options.put("microsoft", "/oauth2/authorization/microsoft");
        options.put("regular", "/api/auth/login");
        return ResponseEntity.ok(options);
    }

    @GetMapping("/success")
    public ResponseEntity<LoginResponse> handleOAuth2Success(@RequestParam String token) {
        return ResponseEntity.ok(new LoginResponse(token, null, null));
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
}
