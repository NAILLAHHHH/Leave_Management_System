package com.ist.leave_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String leaveType;
    private Integer totalDays;
    private Double usedDays;
    private Double pendingDays;
    private Double remainingDays;
    private Double availableDays;
    private Double carriedForwardDays;
    private Integer year;
} 