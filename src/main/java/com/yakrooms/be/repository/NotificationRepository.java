package com.yakrooms.be.repository;

import com.yakrooms.be.model.entity.Notification;
import com.yakrooms.be.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);
    List<Notification> findByUserAndIsReadFalse(User user);
    
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.user LEFT JOIN FETCH n.room WHERE n.user = :user")
    List<Notification> findByUserWithAssociations(@Param("user") User user);
    
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.user LEFT JOIN FETCH n.room WHERE n.user.id = :userId AND n.isRead = false")
    List<Notification> findByUserAndIsReadFalseWithAssociations(@Param("userId") Long userId);
    
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.user LEFT JOIN FETCH n.room WHERE n.user.id = :userId")
    List<Notification> findByUserIdWithAssociations(@Param("userId") Long userId);
} 