package com.ist.leave_management_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String profilePictureUrl;
    private String password;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id")
    private UserRole role;
    
}
