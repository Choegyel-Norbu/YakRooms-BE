package com.yakrooms.be.service.impl;

import com.yakrooms.be.model.entity.Notification;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.NotificationType;
import com.yakrooms.be.repository.NotificationRepository;
import com.yakrooms.be.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getAllNotifications(User user) {
        return notificationRepository.findByUserWithAssociations(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Notification> getAllNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserAndIsReadFalseWithAssociations(userId);
    }

    @Override
    @Transactional
    public Notification createNotification(User user, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type.name());
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByUserAndIsReadFalse(user);
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional
    public void deleteAllNotifications(User user) {
        List<Notification> all = notificationRepository.findByUser(user);
        notificationRepository.deleteAll(all);
    }
} 