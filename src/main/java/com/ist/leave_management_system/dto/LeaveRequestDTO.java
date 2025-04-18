package com.ist.leave_management_system.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveRequestDTO {
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isHalfDay;
    private String reason;
    private String supportingDocumentPath;
} 