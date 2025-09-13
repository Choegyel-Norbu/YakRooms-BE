package com.yakrooms.be.service;

import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.entity.Notification;
import com.yakrooms.be.model.entity.User;
import java.util.List;

public interface NotificationService {
    // ========== BASIC NOTIFICATION OPERATIONS ==========
    List<Notification> getAllNotifications(User user);
    List<Notification> getAllNotificationsByUserId(Long userId);
    void markAllAsRead(User user);
    void deleteAllNotifications(User user);
    void deleteNotificationById(Long notificationId);
    void deleteNotificationsByIds(List<Long> notificationIds);
    
    // ========== UNIFIED NOTIFICATION CREATION ==========
    
    /**
     * Creates notifications for both guest and hotel owner when a new booking is created
     * @param booking The booking that was created
     * @return List containing both guest and hotel notifications
     */
    List<Notification> createBookingNotifications(Booking booking);
    
    /**
     * Creates notifications for both guest and hotel owner when a cancellation is requested
     * @param booking The booking for which cancellation is requested
     * @return List containing both guest and hotel notifications
     */
    List<Notification> createCancellationRequestNotifications(Booking booking);
    
    /**
     * Creates a notification when a cancellation request is rejected by hotel staff
     * @param booking The booking for which cancellation was rejected
     * @return The created notification
     */
    Notification createCancellationRejectionNotification(Booking booking);
    
    /**
     * Creates a notification when a cancellation request is approved by hotel staff
     * @param booking The booking for which cancellation was approved
     * @return The created notification
     */
    Notification createCancellationApprovalNotification(Booking booking);
    
    /**
     * Gets all notifications with type HOTEL_DELETION_REQUEST
     * @return List of hotel deletion request notifications
     */
    List<Notification> getAllHotelDeletionRequestNotifications();
} 