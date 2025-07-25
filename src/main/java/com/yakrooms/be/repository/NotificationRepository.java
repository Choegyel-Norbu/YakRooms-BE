package com.yakrooms.be.repository;

import com.yakrooms.be.model.entity.Notification;
import com.yakrooms.be.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);
    List<Notification> findByUserAndIsReadFalse(User user);
} 