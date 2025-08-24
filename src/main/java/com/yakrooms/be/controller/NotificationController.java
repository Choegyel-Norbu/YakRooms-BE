package com.yakrooms.be.controller;

import com.yakrooms.be.dto.NotificationDTO;
import com.yakrooms.be.dto.mapper.NotificationMapper;
import com.yakrooms.be.model.entity.Notification;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserRepository userRepository;

    // Fetch all notifications for a user - All authenticated users can access their own notifications
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOTEL_ADMIN', 'STAFF', 'GUEST')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getAllNotifications(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getAllNotificationsByUserId(userId);
        List<NotificationDTO> dtos = notifications.stream().map(NotificationMapper::toDto).toList();
        return ResponseEntity.ok(dtos);
    }

    // Mark all unread notifications as read for a user - All authenticated users can mark their own notifications as read
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOTEL_ADMIN', 'STAFF', 'GUEST')")
    @PutMapping("/user/{userId}/markAllRead")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        User user = userRepository.findByIdWithCollections(userId).orElseThrow(() -> new RuntimeException("User not found"));
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }

    // Delete all notifications for a user - All authenticated users can delete their own notifications
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOTEL_ADMIN', 'STAFF', 'GUEST')")
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteAllNotifications(@PathVariable Long userId) {
        User user = userRepository.findByIdWithCollections(userId).orElseThrow(() -> new RuntimeException("User not found"));
        notificationService.deleteAllNotifications(user);
        return ResponseEntity.noContent().build();
    }
} 