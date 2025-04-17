package com.ist.leave_management_system.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // who the notification is for
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee recipient;

    private String message;
    private boolean readFlag;
    private LocalDateTime createdAt;
}
