package com.ist.leave_management_system.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    // @Pattern(
    //     regexp = "^[A-Za-z0-9._%+-]+@ist\\.com$", 
    //     flags = Pattern.Flag.CASE_INSENSITIVE,
    //     message = "Email must be a valid @ist.com address"
    // )
    private String email;

    private String profilePictureUrl;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Role name is required")
    private String roleName;
}
