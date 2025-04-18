package com.ist.leave_management_system.dto;

import com.ist.leave_management_system.model.LeaveStatus;
import com.ist.leave_management_system.model.LeaveType;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveResponseDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LeaveType leaveType;
    private LeaveStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isHalfDay;
    private String reason;
    private String supportingDocumentPath;
    private String approvedBy;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 