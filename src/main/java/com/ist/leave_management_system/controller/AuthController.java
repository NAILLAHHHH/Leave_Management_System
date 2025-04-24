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
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.ist.leave_management_system.config.JwtUtils;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    public AuthController(AuthService authService, JwtUtils jwtUtils) {
        this.authService = authService;
        this.jwtUtils = jwtUtils;
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

     /**
     * Initiate Microsoft OAuth Login
     */
    @GetMapping("/microsoft/login")
    public ResponseEntity<Void> microsoftLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/microsoft");
        return ResponseEntity.ok().build();
    }
    
    /**
     * Handle OAuth2 redirect and token validation
     */
    @GetMapping("/oauth2/token")
    public ResponseEntity<LoginResponse> validateOAuthToken(@RequestParam("token") String token) {
        if (jwtUtils.validateToken(token)) {
            String email = jwtUtils.getEmailFromToken(token);
            Employee employee = authService.findByEmail(email);
            return ResponseEntity.ok(new LoginResponse(token, email, employee.getRole().getRoleName()));
        }
        return ResponseEntity.badRequest().build();
    }
}

