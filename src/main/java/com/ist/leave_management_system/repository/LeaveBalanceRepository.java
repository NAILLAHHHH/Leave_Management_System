package com.ist.leave_management_system.repository;

import com.ist.leave_management_system.model.LeaveBalance;
import com.ist.leave_management_system.model.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeEntityAndYear(Long employeeId, LeaveType leaveType, int year);
    List<LeaveBalance> findByEmployeeId(Long employeeId);
    List<LeaveBalance> findByEmployeeIdAndYear(Long employeeId, int year);
} 