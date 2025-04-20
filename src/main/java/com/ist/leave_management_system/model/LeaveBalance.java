package com.ist.leave_management_system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "leave_balances")
public class LeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveTypeEntity;

    @Column(name = "leave_type", nullable = false)
    private String leaveType;

    @Column(nullable = false)
    private double totalDays;

    @Column(nullable = false)
    private double usedDays = 0.0;

    @Column(nullable = false)
    private double pendingDays = 0.0;

    @Column(nullable = false)
    private double remainingDays;

    @Column(nullable = false)
    private double carriedForwardDays = 0.0;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (leaveTypeEntity == null || leaveTypeEntity.getId() == null) {
            throw new IllegalStateException("Leave type must be set and have an ID before saving LeaveBalance");
        }
        if (employee == null || employee.getId() == null) {
            throw new IllegalStateException("Employee must be set and have an ID before saving LeaveBalance");
        }
        if (leaveType == null) {
            leaveType = leaveTypeEntity.getName();
        }
        createdAt = LocalDateTime.now();
        calculateRemainingDays();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateRemainingDays();
    }

    private void calculateRemainingDays() {
        this.remainingDays = this.totalDays + this.carriedForwardDays - this.usedDays;
    }

    public double getAvailableDays() {
        return this.remainingDays;
    }
} 