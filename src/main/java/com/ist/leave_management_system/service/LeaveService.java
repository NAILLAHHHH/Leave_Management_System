package com.ist.leave_management_system.service;

import com.ist.leave_management_system.dto.LeaveRequestDTO;
import com.ist.leave_management_system.dto.LeaveResponseDTO;
import com.ist.leave_management_system.model.Employee;
import com.ist.leave_management_system.model.LeaveStatus;

import java.util.List;

public interface LeaveService {
    LeaveResponseDTO applyForLeave(String email, LeaveRequestDTO leaveRequest);
    LeaveResponseDTO approveLeave(Long leaveId, String approverEmail, String comment);
    LeaveResponseDTO rejectLeave(Long leaveId, String approverEmail, String reason);
    LeaveResponseDTO cancelLeave(Long leaveId, String email);
    List<LeaveResponseDTO> getEmployeeLeaves(String email);
    List<LeaveResponseDTO> getPendingLeavesForApproval(String approverEmail);
    LeaveResponseDTO getLeaveById(Long leaveId);
    List<LeaveResponseDTO> getLeavesByStatus(LeaveStatus status);
    List<LeaveResponseDTO> getAllLeaves();
    List<LeaveResponseDTO> getPendingLeavesForApproval();
    void initializeLeaveBalancesForEmployee(Employee employee);
    List<LeaveResponseDTO> getLeavesByEmployeeId(Long employeeId);
    List<LeaveResponseDTO> getEmployeesCurrentlyOnLeave();
} 