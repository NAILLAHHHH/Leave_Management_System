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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
