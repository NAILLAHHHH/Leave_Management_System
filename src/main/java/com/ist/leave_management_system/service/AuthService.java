package com.ist.leave_management_system.service;

import com.ist.leave_management_system.config.JwtUtils;
import com.ist.leave_management_system.dto.LoginResponse;
import com.ist.leave_management_system.dto.RegisterRequest;
import com.ist.leave_management_system.model.Employee;
import com.ist.leave_management_system.model.UserRole;
import com.ist.leave_management_system.repository.EmployeeRepository;
import com.ist.leave_management_system.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
public class AuthService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    public Employee registerUser(RegisterRequest request) {
        // Check if email already exists
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already taken");
        }

        // Check if role exists
        UserRole role = userRoleRepository.findByRoleName(request.getRoleName());
        if (role == null) {
            throw new RuntimeException("Role not found: " + request.getRoleName());
        }

        // Create the new user
        Employee user = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .profilePictureUrl(request.getProfilePictureUrl())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        return employeeRepository.save(user);
    }

    public LoginResponse loginUser(String email, String password) {
        Employee user = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtils.generateToken(user.getEmail());
        return new LoginResponse(token, user.getEmail(), user.getRole().getRoleName());
    }

    /**
 * Find an employee by email
 * 
 * @param email the email to search for
 * @return the found Employee
 */
public Employee findByEmail(String email) {
    return employeeRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
}
}
