package com.ist.leave_management_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** A machine‐friendly name (e.g. "ADMIN", "MANAGER", "STAFF") **/
    @Column(nullable = false, unique = true)
    private String roleName;

    /** A human‐friendly description **/
    private String description;
}