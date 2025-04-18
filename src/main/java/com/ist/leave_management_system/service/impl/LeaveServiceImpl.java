package com.ist.leave_management_system.service.impl;

import com.ist.leave_management_system.dto.LeaveRequestDTO;
import com.ist.leave_management_system.dto.LeaveResponseDTO;
import com.ist.leave_management_system.exception.ResourceNotFoundException;
import com.ist.leave_management_system.model.*;
import com.ist.leave_management_system.repository.LeaveRepository;
import com.ist.leave_management_system.repository.LeaveBalanceRepository;
import com.ist.leave_management_system.repository.LeaveTypeRepository;
import com.ist.leave_management_system.service.LeaveService;
import com.ist.leave_management_system.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeService employeeService;
    private final LeaveTypeRepository leaveTypeRepository;

    @Override
    @Transactional
    public LeaveResponseDTO applyForLeave(String email, LeaveRequestDTO leaveRequest) {
        try {
            System.out.println("Starting leave application process for email: " + email);
            
            Employee employee = employeeService.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
            System.out.println("Found employee with ID: " + employee.getId());

            if (leaveRequest.getLeaveTypeName() == null || leaveRequest.getLeaveTypeName().trim().isEmpty()) {
                throw new IllegalArgumentException("Leave type name cannot be null or empty");
            }

            LeaveType leaveType = leaveTypeRepository.findByNameIgnoreCase(leaveRequest.getLeaveTypeName().trim().toUpperCase())
                    .orElseThrow(() -> {
                        List<LeaveType> allTypes = leaveTypeRepository.findAll();
                        String availableTypes = allTypes.stream()
                                .map(LeaveType::getName)
                                .collect(Collectors.joining(", "));
                        return new ResourceNotFoundException(
                            String.format("Leave type '%s' not found. Available types are: %s", 
                                leaveRequest.getLeaveTypeName(), 
                                availableTypes)
                        );
                    });
            System.out.println("Found leave type with ID: " + leaveType.getId());

            // Check leave balance
            LeaveBalance leaveBalance = getOrCreateLeaveBalance(employee, leaveType);
            double requestedDays = calculateRequestedDays(leaveRequest);
            
            // Check against available days (remaining - pending)
            if (leaveBalance.getAvailableDays() < requestedDays) {
                throw new IllegalArgumentException(
                    String.format("Insufficient leave balance. Available: %.1f, Requested: %.1f, Pending: %.1f", 
                        leaveBalance.getAvailableDays(), 
                        requestedDays,
                        leaveBalance.getPendingDays())
                );
            }

            Leave leave = new Leave();
            leave.setEmployee(employee);
            leave.setLeaveType(leaveType);
            leave.setLeaveTypeName(leaveType.getName());
            leave.setStartDate(leaveRequest.getStartDate());
            leave.setEndDate(leaveRequest.getEndDate());
            leave.setHalfDay(leaveRequest.isHalfDay());
            leave.setReason(leaveRequest.getReason());
            leave.setSupportingDocumentPath(leaveRequest.getSupportingDocumentPath());
            leave.setStatus(LeaveStatus.PENDING);
            leave.setCreatedAt(LocalDateTime.now());

            // Save leave request
            Leave savedLeave = leaveRepository.saveAndFlush(leave);
            
            // Update pending days in balance
            leaveBalance.setPendingDays(leaveBalance.getPendingDays() + requestedDays);
            leaveBalanceRepository.save(leaveBalance);
            
            System.out.println("Successfully saved leave request with ID: " + savedLeave.getId());
            System.out.println("Updated leave balance - Pending: " + leaveBalance.getPendingDays() + 
                             ", Available: " + leaveBalance.getAvailableDays());
            
            return convertToDTO(savedLeave);
        } catch (Exception e) {
            System.err.println("Error in applyForLeave:");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save leave request: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public LeaveResponseDTO approveLeave(Long leaveId, String approverEmail, String comment) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));

        Employee approver = employeeService.findByEmail(approverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Leave is not in pending status");
        }

        // Calculate days and update balance
        double requestedDays = calculateRequestedDays(leave);
        LeaveBalance leaveBalance = getOrCreateLeaveBalance(leave.getEmployee(), leave.getLeaveType());
        
        // Move days from pending to used
        leaveBalance.setPendingDays(leaveBalance.getPendingDays() - requestedDays);
        leaveBalance.setUsedDays(leaveBalance.getUsedDays() + requestedDays);
        leaveBalanceRepository.save(leaveBalance);

        // Update leave status
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedBy(approver);
        leave.setRejectionReason(null);

        Leave updatedLeave = leaveRepository.save(leave);
        return convertToDTO(updatedLeave);
    }

    @Override
    @Transactional
    public LeaveResponseDTO rejectLeave(Long leaveId, String approverEmail, String reason) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));

        Employee approver = employeeService.findByEmail(approverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Leave is not in pending status");
        }

        // Remove pending days from balance
        double requestedDays = calculateRequestedDays(leave);
        LeaveBalance leaveBalance = getOrCreateLeaveBalance(leave.getEmployee(), leave.getLeaveType());
        leaveBalance.setPendingDays(leaveBalance.getPendingDays() - requestedDays);
        leaveBalanceRepository.save(leaveBalance);

        // Update leave status
        leave.setStatus(LeaveStatus.REJECTED);
        leave.setApprovedBy(approver);
        leave.setRejectionReason(reason);

        Leave updatedLeave = leaveRepository.save(leave);
        return convertToDTO(updatedLeave);
    }

    @Override
    @Transactional
    public LeaveResponseDTO cancelLeave(Long leaveId, String email) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));

        Employee employee = employeeService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (!leave.getEmployee().getId().equals(employee.getId())) {
            throw new IllegalArgumentException("You can only cancel your own leaves");
        }

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only pending leaves can be cancelled");
        }

        // Remove pending days from balance
        double requestedDays = calculateRequestedDays(leave);
        LeaveBalance leaveBalance = getOrCreateLeaveBalance(employee, leave.getLeaveType());
        leaveBalance.setPendingDays(leaveBalance.getPendingDays() - requestedDays);
        leaveBalanceRepository.save(leaveBalance);

        // Update leave status
        leave.setStatus(LeaveStatus.CANCELLED);
        Leave updatedLeave = leaveRepository.save(leave);
        return convertToDTO(updatedLeave);
    }

    @Override
    public List<LeaveResponseDTO> getEmployeeLeaves(String email) {
        Employee employee = employeeService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return leaveRepository.findByEmployeeId(employee.getId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveResponseDTO> getPendingLeavesForApproval(String approverEmail) {
        Employee approver = employeeService.findByEmail(approverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));
        return leaveRepository.findByStatus(LeaveStatus.PENDING)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveResponseDTO> getPendingLeavesForApproval() {
        List<Leave> pendingLeaves = leaveRepository.findByStatus(LeaveStatus.PENDING);
        return pendingLeaves.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveResponseDTO> getAllLeaves() {
        List<Leave> allLeaves = leaveRepository.findAll();
        return allLeaves.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LeaveResponseDTO getLeaveById(Long leaveId) {
        return leaveRepository.findById(leaveId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));
    }

    @Override
    public List<LeaveResponseDTO> getLeavesByStatus(LeaveStatus status) {
        List<Leave> leaves = leaveRepository.findByStatus(status);
        return leaves.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    public List<LeaveResponseDTO> getLeavesByEmployeeId(Long employeeId) {
        List<Leave> leaves = leaveRepository.findByEmployeeId(employeeId);
        return leaves.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    private LeaveBalance getOrCreateLeaveBalance(Employee employee, LeaveType leaveType) {
        if (employee == null || employee.getId() == null) {
            throw new IllegalArgumentException("Employee must be set and have an ID");
        }
        if (leaveType == null || leaveType.getId() == null) {
            throw new IllegalArgumentException("Leave type must be set and have an ID");
        }

        System.out.println("Creating/Getting leave balance for:");
        System.out.println("Employee ID: " + employee.getId());
        System.out.println("Leave Type ID: " + leaveType.getId());
        System.out.println("Leave Type Name: " + leaveType.getName());
        
        int currentYear = LocalDate.now().getYear();
        
        // First try to find existing balance
        Optional<LeaveBalance> existingBalance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeEntityAndYear(
            employee.getId(), leaveType, currentYear);
            
        if (existingBalance.isPresent()) {
            System.out.println("Found existing balance");
            return existingBalance.get();
        }

        // Create new balance
        System.out.println("Creating new balance");
        LeaveBalance newBalance = new LeaveBalance();
        
        // Convert the leave type name to the correct enum value
        String leaveTypeName = leaveType.getName().toUpperCase();
        // The leave type name should already be in the correct format since we created it that way
        if (!leaveTypeName.equals("PTO") && 
            !leaveTypeName.equals("SICK_LEAVE") && 
            !leaveTypeName.equals("COMPASSIONATE_LEAVE") && 
            !leaveTypeName.equals("MATERNITY_LEAVE")) {
            throw new IllegalArgumentException("Invalid leave type: " + leaveTypeName + 
                ". Must be one of: PTO, SICK_LEAVE, COMPASSIONATE_LEAVE, MATERNITY_LEAVE");
        }
        
        // Set both leave type fields
        newBalance.setLeaveTypeEntity(leaveType);
        newBalance.setLeaveType(leaveTypeName);
        
        // Set other fields
        newBalance.setEmployee(employee);
        newBalance.setYear(currentYear);
        newBalance.setTotalDays(leaveType.getMaxDaysPerYear());
        newBalance.setUsedDays(0.0);
        newBalance.setRemainingDays(leaveType.getMaxDaysPerYear());
        newBalance.setCarriedForwardDays(0.0);
        newBalance.setCreatedAt(LocalDateTime.now());

        try {
            System.out.println("Saving new balance with:");
            System.out.println("Employee ID: " + newBalance.getEmployee().getId());
            System.out.println("Leave Type ID: " + newBalance.getLeaveTypeEntity().getId());
            System.out.println("Leave Type Name: " + newBalance.getLeaveType());
            
            // Save and flush to ensure immediate persistence
            LeaveBalance savedBalance = leaveBalanceRepository.saveAndFlush(newBalance);
            System.out.println("Successfully created new balance with ID: " + savedBalance.getId());
            return savedBalance;
        } catch (Exception e) {
            System.err.println("Error creating leave balance: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create leave balance: " + e.getMessage(), e);
        }
    }

    private double calculateRequestedDays(LeaveRequestDTO leaveRequest) {
        if (leaveRequest.isHalfDay()) {
            return 0.5;
        }
        return leaveRequest.getStartDate().until(leaveRequest.getEndDate()).getDays() + 1;
    }

    private double calculateRequestedDays(Leave leave) {
        if (leave.isHalfDay()) {
            return 0.5;
        }
        return leave.getStartDate().until(leave.getEndDate()).getDays() + 1;
    }

    private LeaveResponseDTO convertToDTO(Leave leave) {
        LeaveResponseDTO dto = new LeaveResponseDTO();
        dto.setId(leave.getId());
        dto.setEmployeeId(leave.getEmployee().getId());
        dto.setEmployeeName(leave.getEmployee().getFirstName() + " " + leave.getEmployee().getLastName());
        dto.setLeaveType(leave.getLeaveType());
        dto.setStatus(leave.getStatus());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setHalfDay(leave.isHalfDay());
        dto.setReason(leave.getReason());
        dto.setSupportingDocumentPath(leave.getSupportingDocumentPath());
        dto.setApprovedBy(leave.getApprovedBy() != null ? 
            leave.getApprovedBy().getFirstName() + " " + leave.getApprovedBy().getLastName() : null);
        dto.setRejectionReason(leave.getRejectionReason());
        dto.setCreatedAt(leave.getCreatedAt());
        dto.setUpdatedAt(leave.getUpdatedAt());
        return dto;
    }
} 