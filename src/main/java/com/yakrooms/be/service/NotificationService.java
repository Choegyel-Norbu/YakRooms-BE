package com.yakrooms.be.service;

import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.entity.Notification;
import com.yakrooms.be.model.entity.User;
import java.util.List;

public interface NotificationService {
    List<Notification> getAllNotifications(User user);
    List<Notification> getAllNotificationsByUserId(Long userId);
    void markAllAsRead(User user);
    void deleteAllNotifications(User user);
    
    /**
     * Creates a notification when a new booking is created
     * @param booking The booking that was created
     * @return The created notification
     */
    Notification createBookingNotification(Booking booking);
    
    /**
     * Creates a notification when a guest requests booking cancellation
     * @param booking The booking for which cancellation is requested
     * @return The created notification
     */
    Notification createCancellationRequestNotification(Booking booking);
    
    /**
     * Creates a notification when a cancellation request is rejected by hotel staff
     * @param booking The booking for which cancellation was rejected
     * @return The created notification
     */
    Notification createCancellationRejectionNotification(Booking booking);
} 