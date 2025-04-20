package com.ist.leave_management_system.service.impl;

import com.ist.leave_management_system.exception.ResourceNotFoundException;
import com.ist.leave_management_system.model.LeaveType;
import com.ist.leave_management_system.repository.LeaveTypeRepository;
import com.ist.leave_management_system.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LeaveTypeServiceImpl implements LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    @Override
    public LeaveType createLeaveType(LeaveType leaveType) {
        if (leaveType.getName() == null || leaveType.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Leave type name cannot be null or empty");
        }
        if (leaveType.getMaxDaysPerYear() == null || leaveType.getMaxDaysPerYear() <= 0) {
            throw new IllegalArgumentException("Max days per year must be a positive number");
        }

        System.out.println("Creating leave type with name: " + leaveType.getName());
        leaveType.setCreatedAt(LocalDateTime.now());
        LeaveType savedType = leaveTypeRepository.save(leaveType);
        System.out.println("Created leave type with ID: " + savedType.getId());
        return savedType;
    }

    @Override
    public List<LeaveType> getAllLeaveTypes() {
        return leaveTypeRepository.findAll();
    }

    @Override
    public LeaveType getLeaveTypeById(Long id) {
        return leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found"));
    }

    @Override
    public LeaveType updateLeaveType(Long id, LeaveType leaveType) {
        LeaveType existingLeaveType = getLeaveTypeById(id);
        existingLeaveType.setName(leaveType.getName());
        existingLeaveType.setMaxDaysPerYear(leaveType.getMaxDaysPerYear());
        existingLeaveType.setRequiresDocument(leaveType.isRequiresDocument());
        existingLeaveType.setUpdatedAt(LocalDateTime.now());
        return leaveTypeRepository.save(existingLeaveType);
    }

    @Override
    public void deleteLeaveType(Long id) {
        LeaveType leaveType = getLeaveTypeById(id);
        leaveTypeRepository.delete(leaveType);
    }
} 