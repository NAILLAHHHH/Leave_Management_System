package com.ist.leave_management_system.controller;

import com.ist.leave_management_system.dto.LeaveRequestDTO;
import com.ist.leave_management_system.dto.LeaveResponseDTO;
import com.ist.leave_management_system.model.Employee;
import com.ist.leave_management_system.model.LeaveStatus;
import com.ist.leave_management_system.repository.EmployeeRepository;
import com.ist.leave_management_system.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final EmployeeRepository employeeRepository;

    @PostMapping
    public ResponseEntity<LeaveResponseDTO> applyForLeave(
            Authentication authentication,
            @RequestBody LeaveRequestDTO leaveRequest) {
        String email = authentication.getName();
        return ResponseEntity.ok(leaveService.applyForLeave(email, leaveRequest));
    }

    @PostMapping("/{leaveId}/approve")
    public ResponseEntity<LeaveResponseDTO> approveLeave(
            Authentication authentication,
            @PathVariable Long leaveId,
            @RequestParam(required = false) String comment) {
        String approverEmail = authentication.getName();
        return ResponseEntity.ok(leaveService.approveLeave(leaveId, approverEmail, comment));
    }

    @PostMapping("/{leaveId}/reject")
    public ResponseEntity<LeaveResponseDTO> rejectLeave(
            Authentication authentication,
            @PathVariable Long leaveId,
            @RequestParam String reason) {
        String approverEmail = authentication.getName();
        return ResponseEntity.ok(leaveService.rejectLeave(leaveId, approverEmail, reason));
    }

    @PostMapping("/{leaveId}/cancel")
    public ResponseEntity<LeaveResponseDTO> cancelLeave(
            Authentication authentication,
            @PathVariable Long leaveId) {
        String email = authentication.getName();
        return ResponseEntity.ok(leaveService.cancelLeave(leaveId, email));
    }

    @GetMapping("/my-leaves")
    public ResponseEntity<List<LeaveResponseDTO>> getMyLeaves(
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(leaveService.getEmployeeLeaves(email));
    }

    @GetMapping("/pending-approval")
    public ResponseEntity<List<LeaveResponseDTO>> getPendingLeavesForApproval(
            Authentication authentication) {
        String approverEmail = authentication.getName();
        return ResponseEntity.ok(leaveService.getPendingLeavesForApproval(approverEmail));
    }

    @GetMapping("/{leaveId}")
    public ResponseEntity<LeaveResponseDTO> getLeaveById(@PathVariable Long leaveId) {
        return ResponseEntity.ok(leaveService.getLeaveById(leaveId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LeaveResponseDTO>> getLeavesByStatus(@PathVariable LeaveStatus status) {
        return ResponseEntity.ok(leaveService.getLeavesByStatus(status));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveResponseDTO>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @PostMapping("/fix-balances")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> fixLeaveBalances() {
        List<Employee> employees = employeeRepository.findAll();
        int fixedCount = 0;
        
        for (Employee employee : employees) {
            try {
                leaveService.initializeLeaveBalancesForEmployee(employee);
                fixedCount++;
            } catch (Exception e) {
                // Log error but continue with other employees
                System.err.println("Error fixing balance for employee " + employee.getEmail() + ": " + e.getMessage());
            }
        }
        
        return ResponseEntity.ok("Fixed leave balances for " + fixedCount + " employees");
    }
} 