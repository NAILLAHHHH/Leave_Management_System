package com.ist.leave_management_system.service.impl;

import com.ist.leave_management_system.model.LeaveBalance;
import com.ist.leave_management_system.model.LeaveType;
import com.ist.leave_management_system.repository.LeaveBalanceRepository;
import com.ist.leave_management_system.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveBalanceServiceImpl {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    private static final double PTO_ACCRUAL_RATE = 1.66; // days per month
    private static final int MAX_CARRY_FORWARD_DAYS = 5;

    @Scheduled(cron = "0 0 0 1 * ?") // Run at midnight on the first day of each month
    @Transactional
    public void processMonthlyAccrual() {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        
        // Only process accrual for the current year
        List<LeaveBalance> ptoBalances = leaveBalanceRepository.findByLeaveTypeAndYear("PTO", currentYear);
        
        for (LeaveBalance balance : ptoBalances) {
            // Add monthly accrual
            balance.setTotalDays(balance.getTotalDays() + PTO_ACCRUAL_RATE);
            balance.setRemainingDays(balance.getRemainingDays() + PTO_ACCRUAL_RATE);
            balance.setUpdatedAt(LocalDateTime.now());
            leaveBalanceRepository.save(balance);
        }
    }

    @Scheduled(cron = "0 0 0 1 1 ?") // Run at midnight on January 1st
    @Transactional
    public void processYearEndCarryForward() {
        int previousYear = LocalDate.now().getYear() - 1;
        int currentYear = LocalDate.now().getYear();
        
        // Get all PTO balances from previous year
        List<LeaveBalance> previousYearBalances = leaveBalanceRepository.findByLeaveTypeAndYear("PTO", previousYear);
        
        for (LeaveBalance previousBalance : previousYearBalances) {
            // Calculate carry forward amount (capped at 5 days)
            double remainingDays = previousBalance.getRemainingDays();
            double carryForwardDays = Math.min(remainingDays, MAX_CARRY_FORWARD_DAYS);
            
            if (carryForwardDays > 0) {
                // Create or update current year's balance
                LeaveBalance currentYearBalance = leaveBalanceRepository
                    .findByEmployeeIdAndLeaveTypeAndYear(previousBalance.getEmployee().getId(), "PTO", currentYear)
                    .orElseGet(() -> {
                        LeaveBalance newBalance = new LeaveBalance();
                        newBalance.setEmployee(previousBalance.getEmployee());
                        newBalance.setLeaveType("PTO");
                        newBalance.setYear(currentYear);
                        newBalance.setTotalDays(20.0); // Standard PTO entitlement
                        newBalance.setUsedDays(0.0);
                        newBalance.setPendingDays(0.0);
                        newBalance.setCreatedAt(LocalDateTime.now());
                        return newBalance;
                    });
                
                // Update carry forward
                currentYearBalance.setCarriedForwardDays(carryForwardDays);
                currentYearBalance.setRemainingDays(currentYearBalance.getTotalDays() + carryForwardDays);
                currentYearBalance.setUpdatedAt(LocalDateTime.now());
                
                leaveBalanceRepository.save(currentYearBalance);
            }
        }
    }
} 