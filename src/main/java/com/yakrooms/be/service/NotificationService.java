package com.yakrooms.be.service;

import com.yakrooms.be.model.entity.Notification;
import com.yakrooms.be.model.entity.User;
import java.util.List;

public interface NotificationService {
    List<Notification> getAllNotifications(User user);
    void markAllAsRead(User user);
    void deleteAllNotifications(User user);
} 