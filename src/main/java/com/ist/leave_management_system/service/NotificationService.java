package com.ist.leave_management_system.service;

import com.ist.leave_management_system.model.Employee;

public interface NotificationService {
    void notifyLeaveRequest(Long leaveId, Employee employee, Employee admin);
    void notifyLeaveApproval(Long leaveId, Employee employee);
    void notifyLeaveRejection(Long leaveId, Employee employee, String reason);
    void markNotificationAsRead(Long notificationId);
}