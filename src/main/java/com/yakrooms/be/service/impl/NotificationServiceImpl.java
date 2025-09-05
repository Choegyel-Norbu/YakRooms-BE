package com.yakrooms.be.service.impl;

import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.entity.Notification;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.NotificationType;
import com.yakrooms.be.repository.NotificationRepository;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;

    // ========== BASIC NOTIFICATION OPERATIONS ==========

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

    // ========== UNIFIED NOTIFICATION CREATION ==========

    @Override
    @Transactional
    public List<Notification> createBookingNotifications(Booking booking) {
        validateBooking(booking);
        
        List<Notification> notifications = new ArrayList<>();
        
        // Create guest notification
        if (booking.getUser() != null) {
            Notification guestNotification = createNotification(
                booking.getUser(),
                booking,
                NotificationType.BOOKING_CREATED,
                "You have created a new booking",
                createBookingMessage(booking, false)
            );
            if (guestNotification != null) {
                notifications.add(guestNotification);
            }
        }
        
        // Create hotel notification
        User hotelAdmin = findHotelAdminUser(booking.getHotel().getId());
        if (hotelAdmin != null) {
            Notification hotelNotification = createNotification(
                hotelAdmin,
                booking,
                NotificationType.HOTEL_BOOKING_CREATED,
                "New Booking Received",
                createBookingMessage(booking, true)
            );
            if (hotelNotification != null) {
                notifications.add(hotelNotification);
            }
        } else {
            logger.warn("No hotel admin found for hotel {} - skipping hotel booking notification", booking.getHotel().getId());
        }
        
        return notifications;
    }

    @Override
    @Transactional
    public List<Notification> createCancellationRequestNotifications(Booking booking) {
        validateBooking(booking);
        validateBookingUser(booking);
        
        List<Notification> notifications = new ArrayList<>();
        
        // Create guest notification
        Notification guestNotification = createNotification(
            booking.getUser(),
            booking,
            NotificationType.BOOKING_CANCELLATION_REQUEST,
            "Booking Cancellation Requested",
            createCancellationRequestMessage(booking, false)
        );
        if (guestNotification != null) {
            notifications.add(guestNotification);
        }
        
        // Create hotel notification
        User hotelAdmin = findHotelAdminUser(booking.getHotel().getId());
        if (hotelAdmin != null) {
            Notification hotelNotification = createNotification(
                hotelAdmin,
                booking,
                NotificationType.HOTEL_CANCELLATION_REQUEST,
                "Cancellation Request Received",
                createCancellationRequestMessage(booking, true)
            );
            if (hotelNotification != null) {
                notifications.add(hotelNotification);
            }
        } else {
            logger.warn("No hotel admin found for hotel {} - skipping hotel cancellation request notification", booking.getHotel().getId());
        }
        
        return notifications;
    }

    @Override
    @Transactional
    public Notification createCancellationRejectionNotification(Booking booking) {
        validateBooking(booking);
        validateBookingUser(booking);
        
        return createNotification(
            booking.getUser(),
            booking,
            NotificationType.BOOKING_CANCELLATION_REJECTED,
            "Cancellation Request Rejected",
            createCancellationRejectionMessage(booking)
        );
    }

    @Override
    @Transactional
    public Notification createCancellationApprovalNotification(Booking booking) {
        validateBooking(booking);
        validateBookingUser(booking);
        
        return createNotification(
            booking.getUser(),
            booking,
            NotificationType.BOOKING_CANCELLATION_APPROVED,
            "Cancellation Request Approved",
            createCancellationApprovalMessage(booking)
        );
    }

    // ========== HELPER METHODS ==========

    /**
     * Unified notification creation method with duplicate prevention
     */
    private Notification createNotification(User user, Booking booking, NotificationType type, String title, String message) {
        if (user == null) {
            logger.warn("Cannot create notification - user is null for booking {}", booking.getId());
            return null;
        }

        // Check for existing notification to prevent duplicates
        Optional<Notification> existingNotification = notificationRepository.findByBookingAndType(booking, type.name());
        if (existingNotification.isPresent()) {
            logger.debug("Notification of type {} already exists for booking {} - returning existing", type, booking.getId());
            return existingNotification.get();
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setBooking(booking);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type.name());
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    /**
     * Find a hotel admin user for the given hotel ID.
     * Prioritizes HOTEL_ADMIN role, falls back to any user associated with the hotel.
     */
    private User findHotelAdminUser(Long hotelId) {
        try {
            // First try to find a user with HOTEL_ADMIN role
            Optional<User> hotelAdmin = userRepository.findByHotelIdAndRole(hotelId, com.yakrooms.be.model.enums.Role.HOTEL_ADMIN);
            if (hotelAdmin.isPresent()) {
                return hotelAdmin.get();
            }
            
            // Fallback: Find any user associated with this hotel
            List<User> hotelUsers = userRepository.findByHotelId(hotelId);
            if (!hotelUsers.isEmpty()) {
                return hotelUsers.get(0); // Return the first user found
            }
        } catch (Exception e) {
            logger.error("Error finding hotel admin for hotel {}: {}", hotelId, e.getMessage());
        }
        return null;
    }

    /**
     * Extract guest name from booking with fallback logic
     */
    private String getGuestName(Booking booking) {
        if (booking.getGuestName() != null && !booking.getGuestName().trim().isEmpty()) {
            return booking.getGuestName();
        }
        if (booking.getUser() != null && booking.getUser().getName() != null && !booking.getUser().getName().trim().isEmpty()) {
            return booking.getUser().getName();
        }
        return "Guest";
    }

    /**
     * Format booking dates consistently
     */
    private String formatBookingDates(Booking booking) {
        String checkInDate = booking.getCheckInDate().format(DATE_FORMATTER);
        String checkOutDate = booking.getCheckOutDate().format(DATE_FORMATTER);
        return String.format("Check-in: %s, Check-out: %s", checkInDate, checkOutDate);
    }

    // ========== VALIDATION METHODS ==========

    private void validateBooking(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }
        if (booking.getHotel() == null) {
            throw new IllegalArgumentException("Booking hotel cannot be null");
        }
    }

    private void validateBookingUser(Booking booking) {
        if (booking.getUser() == null) {
            throw new IllegalArgumentException("Booking user cannot be null");
        }
    }

    // ========== MESSAGE TEMPLATES ==========

    private String createBookingMessage(Booking booking, boolean isForHotel) {
        String guestName = getGuestName(booking);
        String dates = formatBookingDates(booking);
        
        if (isForHotel) {
            return String.format(
                "A new booking has been received for %s at %s. " +
                "Guest: %s, %s, Guests: %d, Room: %s, Passcode: %s",
                guestName, booking.getHotel().getName(), guestName, dates,
                booking.getGuests(), booking.getRoom().getRoomNumber(), booking.getPasscode()
            );
        } else {
            return String.format(
                "A new booking has been created for %s at %s. " +
                "%s, Guests: %d, Passcode: %s",
                guestName, booking.getHotel().getName(), dates,
                booking.getGuests(), booking.getPasscode()
            );
        }
    }

    private String createCancellationRequestMessage(Booking booking, boolean isForHotel) {
        String guestName = getGuestName(booking);
        String dates = formatBookingDates(booking);
        
        if (isForHotel) {
            return String.format(
                "A guest has requested cancellation for booking at %s. " +
                "Guest: %s, %s, Room: %s, Passcode: %s. " +
                "Please review and approve or reject this cancellation request.",
                booking.getHotel().getName(), guestName, dates,
                booking.getRoom().getRoomNumber(), booking.getPasscode()
            );
        } else {
            return String.format(
                "Cancellation has been requested for booking at %s. " +
                "%s, Guest: %s, Passcode: %s. " +
                "Please review and process the cancellation request.",
                booking.getHotel().getName(), dates, guestName, booking.getPasscode()
            );
        }
    }

    private String createCancellationRejectionMessage(Booking booking) {
        String dates = formatBookingDates(booking);
        return String.format(
            "Your cancellation request for booking at %s has been rejected. " +
            "Your booking remains active. %s, Passcode: %s. " +
            "Please contact the hotel directly if you have any questions.",
            booking.getHotel().getName(), dates, booking.getPasscode()
        );
    }

    private String createCancellationApprovalMessage(Booking booking) {
        String dates = formatBookingDates(booking);
        return String.format(
            "Your cancellation request for booking at %s has been approved. " +
            "Your booking has been cancelled. %s, Passcode: %s. " +
            "If you have any questions, please contact the hotel directly.",
            booking.getHotel().getName(), dates, booking.getPasscode()
        );
    }
} 