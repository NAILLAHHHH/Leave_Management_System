package com.ist.leave_management_system.service.impl;

import com.ist.leave_management_system.model.Employee;
import com.ist.leave_management_system.model.Notification;
import com.ist.leave_management_system.repository.NotificationRepository;
import com.ist.leave_management_system.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public void notifyLeaveRequest(Long leaveId, Employee employee, Employee admin) {
        Notification notification = new Notification();
        notification.setEmployee(admin);
        notification.setTitle("New Leave Request");
        notification.setMessage(String.format("Employee %s %s has submitted a new leave request (#%d)", 
                employee.getFirstName(), employee.getLastName(), leaveId));
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public void notifyLeaveApproval(Long leaveId, Employee employee) {
        Notification notification = new Notification();
        notification.setEmployee(employee);
        notification.setTitle("Leave Request Approved");
        notification.setMessage(String.format("Your leave request (#%d) has been approved", leaveId));
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public void notifyLeaveRejection(Long leaveId, Employee employee, String reason) {
        Notification notification = new Notification();
        notification.setEmployee(employee);
        notification.setTitle("Leave Request Rejected");
        notification.setMessage(String.format("Your leave request (#%d) has been rejected. Reason: %s", 
                leaveId, reason));
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public void markNotificationAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        });
    }
}