package com.yakrooms.be.service.impl;

import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.entity.Notification;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.NotificationType;
import com.yakrooms.be.repository.NotificationRepository;
import com.yakrooms.be.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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

    @Override
    @Transactional
    public Notification createBookingNotification(Booking booking) {
        if (booking == null || booking.getUser() == null) {
            throw new IllegalArgumentException("Booking and user cannot be null");
        }

        // Check if a booking created notification already exists for this booking
        Optional<Notification> existingNotification = notificationRepository.findByBookingAndType(
            booking, 
            NotificationType.BOOKING_CREATED.name()
        );
        
        if (existingNotification.isPresent()) {
            // Return existing notification instead of creating a duplicate
            return existingNotification.get();
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String checkInDate = booking.getCheckInDate().format(dateFormatter);
        String checkOutDate = booking.getCheckOutDate().format(dateFormatter);
        
        String title = "New Booking Created";
        String message = String.format(
            "A new booking has been created for %s at %s. " +
            "Check-in: %s, Check-out: %s, Guests: %d, Passcode: %s",
            booking.getGuestName() != null ? booking.getGuestName() : "Guest",
            booking.getHotel().getName(),
            checkInDate,
            checkOutDate,
            booking.getGuests(),
            booking.getPasscode()
        );

        Notification notification = new Notification();
        notification.setUser(booking.getUser());
        notification.setBooking(booking);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(NotificationType.BOOKING_CREATED.name());
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public Notification createCancellationRequestNotification(Booking booking) {
        if (booking == null || booking.getUser() == null) {
            throw new IllegalArgumentException("Booking and user cannot be null");
        }

        // Check if a cancellation request notification already exists for this booking
        Optional<Notification> existingNotification = notificationRepository.findByBookingAndType(
            booking, 
            NotificationType.BOOKING_CANCELLATION_REQUEST.name()
        );
        
        if (existingNotification.isPresent()) {
            // Return existing notification instead of creating a duplicate
            return existingNotification.get();
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String checkInDate = booking.getCheckInDate().format(dateFormatter);
        String checkOutDate = booking.getCheckOutDate().format(dateFormatter);
        
        String title = "Booking Cancellation Requested";
        String message = String.format(
            "Cancellation has been requested for booking at %s. " +
            "Original check-in: %s, Check-out: %s, Guest: %s, Passcode: %s. " +
            "Please review and process the cancellation request.",
            booking.getHotel().getName(),
            checkInDate,
            checkOutDate,
            booking.getGuestName() != null ? booking.getGuestName() : "Guest",
            booking.getPasscode()
        );

        Notification notification = new Notification();
        notification.setUser(booking.getUser());
        notification.setBooking(booking);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(NotificationType.BOOKING_CANCELLATION_REQUEST.name());
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }
} 