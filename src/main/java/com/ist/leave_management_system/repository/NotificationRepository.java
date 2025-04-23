package com.ist.leave_management_system.repository;

import com.ist.leave_management_system.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByEmployeeIdAndReadOrderByCreatedAtDesc(Long employeeId, boolean read);
    List<Notification> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
}