package com.yakrooms.be.service.impl;

import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.entity.Notification;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.NotificationType;
import com.yakrooms.be.repository.NotificationRepository;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.service.NotificationService;
import com.yakrooms.be.dto.NotificationMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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
        notificationRepository.deleteBookingCreatedByUserId(user.getId());
    }

    @Override
    @Transactional
    public Notification createBookingNotification(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }

        // If booking has no user, don't create notification
        if (booking.getUser() == null) {
            return null;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String checkInDate = booking.getCheckInDate().format(dateFormatter);
        String checkOutDate = booking.getCheckOutDate().format(dateFormatter);
        
        String title = "You have a new booking";
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

    @Override
    @Transactional
    public Notification createCancellationRejectionNotification(Booking booking) {
        if (booking == null || booking.getUser() == null) {
            throw new IllegalArgumentException("Booking and user cannot be null");
        }

        // Check if a cancellation rejection notification already exists for this booking
        Optional<Notification> existingNotification = notificationRepository.findByBookingAndType(
            booking, 
            NotificationType.BOOKING_CANCELLATION_REJECTED.name()
        );
        
        if (existingNotification.isPresent()) {
            // Return existing notification instead of creating a duplicate
            return existingNotification.get();
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String checkInDate = booking.getCheckInDate().format(dateFormatter);
        String checkOutDate = booking.getCheckOutDate().format(dateFormatter);
        
        String title = "Cancellation Request Rejected";
        String message = String.format(
            "Your cancellation request for booking at %s has been rejected. " +
            "Your booking remains active. Check-in: %s, Check-out: %s, Passcode: %s. " +
            "Please contact the hotel directly if you have any questions.",
            booking.getHotel().getName(),
            checkInDate,
            checkOutDate,
            booking.getPasscode()
        );

        Notification notification = new Notification();
        notification.setUser(booking.getUser());
        notification.setBooking(booking);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(NotificationType.BOOKING_CANCELLATION_REJECTED.name());
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    /**
     * Find a hotel admin user for the given hotel ID.
     * This is a simplified implementation - in production, you might want to
     * notify all hotel staff or have a more sophisticated user role system.
     * 
     * @param hotelId The hotel ID
     * @return A hotel admin user, or null if none found
     */
    private User findHotelAdminUser(Long hotelId) {
        try {
            // Find the first user associated with this hotel
            // In a more sophisticated system, you'd look for users with ADMIN role
            List<User> hotelUsers = userRepository.findByHotelId(hotelId);
            if (!hotelUsers.isEmpty()) {
                return hotelUsers.get(0); // Return the first user found
            }
        } catch (Exception e) {
            // Log the error but don't fail the notification creation
            logger.error("Error finding hotel admin for hotel {}: {}", hotelId, e.getMessage());
        }
        return null;
    }
    
    /**
     * Send WebSocket notification to a specific user
     * 
     * @param userId The user ID to send notification to
     * @param payload The notification message payload
     */
    public void notifyUser(String userId, NotificationMessage payload) {
        String destination = "/topic/notifications/" + userId;
        logger.info("=== SENDING WEBSOCKET MESSAGE ===");
        logger.info("Destination: {}", destination);
        logger.info("Payload: {}", payload);
        
        try {
            messagingTemplate.convertAndSend(destination, payload);
            logger.info("WebSocket message sent successfully to user: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to send WebSocket message to user {}: {}", userId, e.getMessage());
            e.printStackTrace();
        }
    }
} 