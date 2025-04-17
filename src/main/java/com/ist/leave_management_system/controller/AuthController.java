package com.ist.leave_management_system.controller;

import com.ist.leave_management_system.model.Employee;
import com.ist.leave_management_system.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public Employee registerUser(@RequestBody Employee user, @RequestParam String roleName) {
        return authService.registerUser(user, roleName);
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestParam String email,
                                            @RequestParam String password) {
        authService.loginUser(email, password);  // will throw if invalid
        return ResponseEntity.ok("You're logged in successfully");
    }
}
