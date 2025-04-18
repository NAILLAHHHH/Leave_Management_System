package com.ist.leave_management_system.repository;

import com.ist.leave_management_system.model.Leave;
import com.ist.leave_management_system.model.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByEmployeeId(Long employeeId);
    List<Leave> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);
    List<Leave> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    List<Leave> findByEmployeeIdAndStartDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);
    List<Leave> findByStatus(LeaveStatus status);
} 