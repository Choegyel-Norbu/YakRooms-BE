package com.yakrooms.be.service;

import com.yakrooms.be.model.entity.Notification;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.NotificationType;
import java.util.List;

public interface NotificationService {
    List<Notification> getAllNotifications(User user);
    List<Notification> getAllNotificationsByUserId(Long userId);
    void markAllAsRead(User user);
    void deleteAllNotifications(User user);
    Notification createNotification(User user, String message, NotificationType type);
} 