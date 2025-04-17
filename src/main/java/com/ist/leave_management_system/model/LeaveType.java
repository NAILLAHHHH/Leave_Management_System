package com.ist.leave_management_system.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer maxDaysPerYear;
    private boolean requiresDocument;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
