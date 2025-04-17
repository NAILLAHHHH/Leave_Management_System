package com.ist.leave_management_system.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "leave_type_id")
    private LeaveType leaveType;

    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String approver;        // name or email of approver
    private String documentUrl;     // link to uploaded doc
    private LocalDateTime submittedAt;
    private LocalDateTime decisionAt;
    private String comments;        // approver comments

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }
}
