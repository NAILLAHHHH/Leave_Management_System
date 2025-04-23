package com.ist.leave_management_system.controller;

import com.ist.leave_management_system.dto.NotificationDTO;
import com.ist.leave_management_system.model.Employee;
import com.ist.leave_management_system.model.Notification;
import com.ist.leave_management_system.repository.NotificationRepository;
import com.ist.leave_management_system.service.EmployeeService;
import com.ist.leave_management_system.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(Authentication authentication) {
        String email = authentication.getName();
        Employee employee = employeeService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        List<Notification> notifications = notificationRepository.findByEmployeeIdOrderByCreatedAtDesc(employee.getId());
        
        List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(notificationDTOs);
    }
    
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(Authentication authentication) {
        String email = authentication.getName();
        Employee employee = employeeService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        List<Notification> notifications = notificationRepository.findByEmployeeIdAndReadOrderByCreatedAtDesc(
                employee.getId(), false);
        
        List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(notificationDTOs);
    }
    
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markNotificationAsRead(id);
        return ResponseEntity.ok().build();
    }
    
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReadAt(notification.getReadAt());
        return dto;
    }
}