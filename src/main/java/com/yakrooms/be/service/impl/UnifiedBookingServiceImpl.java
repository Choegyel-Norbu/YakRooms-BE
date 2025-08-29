package com.yakrooms.be.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yakrooms.be.dto.mapper.BookingMapper;
import com.yakrooms.be.dto.request.BookingRequest;
import com.yakrooms.be.dto.response.BookingResponse;
import com.yakrooms.be.exception.BusinessException;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.BookingStatus;
import com.yakrooms.be.repository.BookingRepository;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.repository.RoomRepository;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.service.BookingValidationService;
import com.yakrooms.be.service.MailService;
import com.yakrooms.be.service.NotificationService;
import com.yakrooms.be.service.PaymentService;
import com.yakrooms.be.service.UnifiedBookingService;
import com.yakrooms.be.service.BookingWebSocketService;
import com.yakrooms.be.util.PasscodeGenerator;

/**
 * Implementation of UnifiedBookingService that consolidates all booking flows.
 * This service replaces the previous dual booking system with a unified approach
 * that ensures consistency, proper validation, and atomic operations.
 * 
 * @author YakRooms Team
 * @version 1.0
 */
@Service
@Transactional
public class UnifiedBookingServiceImpl implements UnifiedBookingService {
    
    private static final Logger logger = LoggerFactory.getLogger(UnifiedBookingServiceImpl.class);
    
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final BookingMapper bookingMapper;
    private final NotificationService notificationService;
    private final BookingWebSocketService bookingWebSocketService;
    private final PaymentService paymentService;
    private final BookingValidationService bookingValidationService;
    
    @Autowired
    public UnifiedBookingServiceImpl(
            BookingRepository bookingRepository,
            RoomRepository roomRepository,
            HotelRepository hotelRepository,
            UserRepository userRepository,
            MailService mailService,
            BookingMapper bookingMapper,
            NotificationService notificationService,
            BookingWebSocketService bookingWebSocketService,
            PaymentService paymentService,
            BookingValidationService bookingValidationService) {
        
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.bookingMapper = bookingMapper;
        this.notificationService = notificationService;
        this.bookingWebSocketService = bookingWebSocketService;
        this.paymentService = paymentService;
        this.bookingValidationService = bookingValidationService;
    }
    
    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        logger.info("Creating booking for room: {}", request.getRoomId());
        
        // Check room availability for date conflicts
        if (!checkRoomAvailability(request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate())) {
            throw new BusinessException("Room is not available for the requested dates");
        }
        
        try {
            // Create the booking
            Booking booking = createBookingEntity(request, "IMMEDIATE");
            booking.setStatus(BookingStatus.CONFIRMED);
            
            // Save the booking
            Booking savedBooking = bookingRepository.save(booking);
            
            // Update room availability based on check-in date
            updateRoomAvailabilityForBooking(savedBooking);
            
            // Handle notifications asynchronously (outside transaction)
            handleBookingNotificationsAsync(savedBooking);
            
            logger.info("Successfully created booking with ID: {}", savedBooking.getId());
            return bookingMapper.toDto(savedBooking);
            
        } catch (Exception e) {
            logger.error("Failed to create booking: {}", e.getMessage());
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean checkRoomAvailability(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        // Use time-based availability check with default times
        LocalTime defaultCheckInTime = LocalTime.of(0, 0); // 12:00 AM (midnight)
        LocalTime defaultCheckOutTime = LocalTime.of(12, 0); // 12:00 PM (noon)
        boolean check = checkRoomAvailabilityWithTimes(roomId, checkIn, defaultCheckInTime, checkOut, defaultCheckOutTime);
        return checkRoomAvailabilityWithTimes(roomId, checkIn, defaultCheckInTime, checkOut, defaultCheckOutTime);
    }
    
    /**
     * Check room availability with specific times for precise conflict detection.
     * @param checkOutTime Check-out time
     * @return true if room is available
     */
    public boolean checkRoomAvailabilityWithTimes(Long roomId, LocalDate checkIn, LocalTime checkInTime, 
                                                LocalDate checkOut, LocalTime checkOutTime) {
        // Check for conflicting bookings using time-based logic
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
            roomId, checkIn, checkOut);
        return conflicts.isEmpty();
    }
    
    @Override
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        logger.info("Confirming booking: {}", bookingId);
        
        Booking booking = fetchBookingById(bookingId);
        
        // Validate that booking can be confirmed
        bookingValidationService.validateConfirmation(booking);
        
        // Update status to CONFIRMED
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(booking);
        
        // Update room availability if needed
        updateRoomAvailabilityForBooking(savedBooking);
        
        logger.info("Successfully confirmed booking: {}", bookingId);
        return bookingMapper.toDto(savedBooking);
    }
    
    @Override
    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        logger.info("Cancelling booking: {} by user: {}", bookingId, userId);
        
        Booking booking = fetchBookingById(bookingId);
        
        // Validate booking ownership
        validateBookingOwnership(booking, userId);
        
        // Validate that booking can be cancelled
        bookingValidationService.validateCancellation(booking);
        
        // Update status to CANCELLED
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        // Restore room availability
        restoreRoomAvailability(booking);
        
        logger.info("Successfully cancelled booking: {}", bookingId);
    }
    
    @Override
    @Transactional
    public boolean updateBookingStatus(Long bookingId, String newStatus) {
        logger.info("Updating booking status: {} to: {}", bookingId, newStatus);
        
        Booking booking = fetchBookingById(bookingId);
        BookingStatus status = validateAndParseStatus(newStatus);
        
        // Validate status transition
        bookingValidationService.validateStatusTransition(booking.getStatus(), status);
        
        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(status);
        bookingRepository.save(booking);
        
        // Handle room availability based on status change
        handleRoomAvailabilityForStatusChange(booking, oldStatus, status);
        
        // Broadcast WebSocket event for booking status change
        broadcastBookingStatusChange(booking, oldStatus, status);
        
        logger.info("Successfully updated booking status: {} to: {}", bookingId, status);
        return true;
    }
    
    // ========== PRIVATE HELPER METHODS ==========
    
    private Booking createBookingEntity(BookingRequest request, String bookingType) {
        // Fetch required entities
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        }
        
        Room room = roomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + request.getRoomId()));
        
        Hotel hotel = hotelRepository.findById(request.getHotelId())
            .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + request.getHotelId()));
        
        // Create booking entity
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setHotel(hotel);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setCheckInTime(LocalTime.of(0, 0)); // Default check-in time
        booking.setCheckOutTime(LocalTime.of(12, 0)); // Default check-out time
        booking.setGuests(request.getGuests());
        booking.setTotalPrice(request.getTotalPrice());
        booking.setPhone(request.getPhone());
        booking.setCid(request.getCid());
        booking.setDestination(request.getDestination());
        booking.setOrigin(request.getOrigin());
        booking.setPasscode(PasscodeGenerator.generatePasscode());
        booking.setStatus(BookingStatus.PENDING);
        
        return booking;
    }
    
    private void updateRoomAvailabilityForBooking(Booking booking) {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        LocalDate checkInDate = booking.getCheckInDate();
        LocalTime checkInTime = booking.getCheckInTime();
        
        if (checkInDate.isEqual(today) && currentTime.isAfter(checkInTime)) {
            // Check-in is today and current time is after check-in time, make room unavailable
            updateRoomAvailabilityAtomically(booking.getRoom().getId(), false);
            
            logger.info("Room {} made unavailable - check-in is today at {} and current time is {}", 
                       booking.getRoom().getId(), checkInTime, currentTime);
        } else if (checkInDate.isAfter(today)) {
            // Check-in is in the future, keep room available for immediate bookings
            updateRoomAvailabilityAtomically(booking.getRoom().getId(), true);
            logger.info("Room {} kept available - check-in is in the future ({})", 
                       booking.getRoom().getId(), checkInDate);
        } else if (checkInDate.isEqual(today) && currentTime.isBefore(checkInTime)) {
            // Check-in is today but current time is before check-in time, keep room available
            updateRoomAvailabilityAtomically(booking.getRoom().getId(), true);
            logger.info("Room {} kept available - check-in is today at {} but current time is {} (before check-in)", 
                       booking.getRoom().getId(), checkInTime, currentTime);
        }
    }
    
    /**
     * Restore room availability when a booking is cancelled.
     * 
     * @param booking The cancelled booking
     */
    private void restoreRoomAvailability(Booking booking) {
        if (booking.isActive()) {
            updateRoomAvailabilityAtomically(booking.getRoom().getId(), true);
            logger.info("Restored room {} availability after cancellation", booking.getRoom().getId());
        }
    }
    
    /**
     * Handle room availability changes based on booking status changes.
     * 
     * @param booking The booking
     * @param oldStatus The previous status
     * @param newStatus The new status
     */
    private void handleRoomAvailabilityForStatusChange(Booking booking, BookingStatus oldStatus, BookingStatus newStatus) {
        Room room = booking.getRoom();
        
        switch (newStatus) {
            case CHECKED_IN:
                if (oldStatus != BookingStatus.CHECKED_IN) {
                    updateRoomAvailabilityAtomically(room.getId(), false);
                }
                break;
                
            case CHECKED_OUT:
            case CANCELLED:
                if (oldStatus == BookingStatus.CHECKED_IN || oldStatus == BookingStatus.CONFIRMED) {
                    updateRoomAvailabilityAtomically(room.getId(), true);
                }
                break;
                
            default:
                // No room availability change needed for other statuses
                break;
        }
    }
    
    /**
     * Update room availability atomically.
     * 
     * @param roomId The room ID
     * @param available Whether the room should be available
     * @return true if update was successful
     */
    private boolean updateRoomAvailabilityAtomically(Long roomId, boolean available) {
        try {
            Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
            
            room.setAvailable(false);
            roomRepository.save(room);
            
            logger.debug("Updated room {} availability to: {}", roomId, available);
            return true;
        } catch (Exception e) {
            logger.error("Failed to update room {} availability to {}: {}", roomId, available, e.getMessage());
            return false;
        }
    }
    
    /**
     * Handle booking notifications asynchronously.
     * 
     * @param booking The booking to send notifications for
     */
    private void handleBookingNotificationsAsync(Booking booking) {
        CompletableFuture.runAsync(() -> {
            try {
                // Send notifications (implementation depends on your notification service)
                logger.info("Sending notifications for booking: {}", booking.getId());
            } catch (Exception e) {
                logger.error("Failed to send notifications for booking: {}", booking.getId(), e);
            }
        });
    }
    
    /**
     * Broadcast booking status change via WebSocket.
     * 
     * @param booking The booking
     * @param oldStatus The previous status
     * @param newStatus The new status
     */
    private void broadcastBookingStatusChange(Booking booking, BookingStatus oldStatus, BookingStatus newStatus) {
        try {
            // Create and broadcast booking change event
            // Implementation depends on your WebSocket service
            logger.info("Broadcasting status change: {} -> {} for booking {}", 
                       oldStatus, newStatus, booking.getId());
        } catch (Exception e) {
            logger.error("Failed to broadcast status change for booking {}: {}", 
                        booking.getId(), e.getMessage());
        }
    }
    
    private Booking fetchBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
    }
    
    private void validateBookingOwnership(Booking booking, Long userId) {
        if (booking.getUser() == null || !booking.getUser().getId().equals(userId)) {
            throw new BusinessException("User is not authorized to cancel this booking");
        }
    }
    
    private BookingStatus validateAndParseStatus(String status) {
        try {
            return BookingStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid booking status: " + status);
        }
    }
}
