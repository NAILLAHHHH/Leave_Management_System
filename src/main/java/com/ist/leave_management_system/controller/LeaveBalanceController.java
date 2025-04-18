package com.ist.leave_management_system.controller;

import com.ist.leave_management_system.dto.LeaveBalanceDTO;
import com.ist.leave_management_system.model.Employee;
import com.ist.leave_management_system.model.LeaveBalance;
import com.ist.leave_management_system.repository.LeaveBalanceRepository;
import com.ist.leave_management_system.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leave-balances")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeService employeeService;

    private LeaveBalanceDTO convertToDTO(LeaveBalance balance) {
        return LeaveBalanceDTO.builder()
                .id(balance.getId())
                .employeeId(balance.getEmployee().getId())
                .employeeName(balance.getEmployee().getFirstName() + " " + balance.getEmployee().getLastName())
                .leaveType(balance.getLeaveTypeEntity().getName())
                .totalDays((int) balance.getTotalDays())
                .usedDays(balance.getUsedDays())
                .pendingDays(balance.getPendingDays())
                .remainingDays(balance.getRemainingDays())
                .availableDays(balance.getAvailableDays())
                .carriedForwardDays(balance.getCarriedForwardDays())
                .year(balance.getYear())
                .build();
    }

    @GetMapping("/my-balances")
    public ResponseEntity<List<LeaveBalanceDTO>> getMyLeaveBalances(Authentication authentication) {
        String email = authentication.getName();
        Employee employee = employeeService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<LeaveBalance> balances = leaveBalanceRepository.findByEmployeeId(employee.getId());
        List<LeaveBalanceDTO> dtos = balances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveBalanceDTO>> getEmployeeLeaveBalances(@PathVariable Long employeeId) {
        List<LeaveBalance> balances = leaveBalanceRepository.findByEmployeeId(employeeId);
        List<LeaveBalanceDTO> dtos = balances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
} 