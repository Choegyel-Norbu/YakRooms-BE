package com.yakrooms.be.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.yakrooms.be.dto.request.BookingRequest;
import com.yakrooms.be.exception.BusinessException;
import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.model.enums.BookingStatus;
import com.yakrooms.be.repository.RoomRepository;
import com.yakrooms.be.service.BookingValidationService;

/**
 * Implementation of BookingValidationService with comprehensive validation logic.
 * This service enforces business rules and ensures data consistency across all booking operations.
 * 
 * @author YakRooms Team
 * @version 1.0
 */
@Service
public class BookingValidationServiceImpl implements BookingValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingValidationServiceImpl.class);
    
    // Business hour constants (configurable via properties)
    private static final LocalTime BUSINESS_HOURS_START = LocalTime.of(6, 0); // 6:00 AM
    private static final LocalTime BUSINESS_HOURS_END = LocalTime.of(22, 0);   // 10:00 PM
    
    // Maximum advance booking days (configurable via properties)
    @Value("${booking.max-advance-days:365}")
    private int maxAdvanceDays;
    
    // Minimum stay duration (configurable via properties)
    @Value("${booking.min-stay-days:1}")
    private int minStayDays;
    
    // Maximum stay duration (configurable via properties)
    @Value("${booking.max-stay-days:30}")
    private int maxStayDays;
    
    private final RoomRepository roomRepository;
    
    // Define valid booking status transitions
    private static final Map<BookingStatus, Set<BookingStatus>> VALID_STATUS_TRANSITIONS = new HashMap<>();
    
    static {
        // PENDING can transition to CONFIRMED, CANCELLED, CANCELLATION_REQUESTED, or CHECKED_IN
        VALID_STATUS_TRANSITIONS.put(BookingStatus.PENDING, 
            Set.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED, BookingStatus.CANCELLATION_REQUESTED, BookingStatus.CHECKED_IN));
        
        // CONFIRMED can transition to CHECKED_IN, CANCELLED, CANCELLATION_REQUESTED, or CHECKED_OUT
        VALID_STATUS_TRANSITIONS.put(BookingStatus.CONFIRMED, 
            Set.of(BookingStatus.CHECKED_IN, BookingStatus.CANCELLED, BookingStatus.CANCELLATION_REQUESTED, BookingStatus.CHECKED_OUT));
        
        // CHECKED_IN can transition to CHECKED_OUT or CANCELLED
        VALID_STATUS_TRANSITIONS.put(BookingStatus.CHECKED_IN, 
            Set.of(BookingStatus.CHECKED_OUT, BookingStatus.CANCELLED));
        
        // CANCELLATION_REQUESTED can transition to CANCELLED, CONFIRMED, or CANCELLATION_REJECTED (approve/reject)
        VALID_STATUS_TRANSITIONS.put(BookingStatus.CANCELLATION_REQUESTED, 
            Set.of(BookingStatus.CANCELLED, BookingStatus.CONFIRMED, BookingStatus.CANCELLATION_REJECTED));
        
        // CHECKED_OUT is a terminal state
        VALID_STATUS_TRANSITIONS.put(BookingStatus.CHECKED_OUT, Set.of());
        
        // CANCELLED is a terminal state
        VALID_STATUS_TRANSITIONS.put(BookingStatus.CANCELLED, Set.of());
        
        // CANCELLATION_REJECTED can transition to CHECKED_IN, CANCELLED, CANCELLATION_REQUESTED, or CHECKED_OUT
        VALID_STATUS_TRANSITIONS.put(BookingStatus.CANCELLATION_REJECTED, 
            Set.of(BookingStatus.CHECKED_IN, BookingStatus.CANCELLED, BookingStatus.CANCELLATION_REQUESTED, BookingStatus.CHECKED_OUT));
    }
    
    public BookingValidationServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }
    
    @Override
    public void validateBookingRequest(BookingRequest request) {
        logger.debug("Validating booking request: {}", request);
        
        if (request == null) {
            throw new IllegalArgumentException("Booking request cannot be null");
        }
        
        // Validate required fields
        if (request.getRoomId() == null) {
            throw new IllegalArgumentException("Room ID is required");
        }
        
        if (request.getHotelId() == null) {
            throw new IllegalArgumentException("Hotel ID is required");
        }
        
        if (request.getCheckInDate() == null) {
            throw new IllegalArgumentException("Check-in date is required");
        }
        
        if (request.getCheckOutDate() == null) {
            throw new IllegalArgumentException("Check-out date is required");
        }
        
        if (request.getGuests() <= 0) {
            throw new IllegalArgumentException("Guest count must be greater than 0");
        }
        
        // Validate date range
        validateDateRange(request.getCheckInDate(), request.getCheckOutDate(), "GENERAL");
        
        // Validate guest count against room capacity
        validateGuestCount(request.getRoomId(), request.getGuests());
        
        // Validate phone number format if provided
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            validatePhoneNumber(request.getPhone());
        }
        
        logger.debug("Booking request validation passed");
    }
    
    @Override
    public void validateDateRange(LocalDate checkIn, LocalDate checkOut, String bookingType) {
        logger.debug("Validating date range: {} to {} for booking type: {}", checkIn, checkOut, bookingType);
        
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out dates cannot be null");
        }
        
        LocalDate today = LocalDate.now();
        
        // Validate check-out is after check-in
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
        
        // Validate minimum stay duration
        long stayDays = checkIn.until(checkOut).getDays();
        if (stayDays < minStayDays) {
            throw new IllegalArgumentException(String.format("Minimum stay duration is %d days", minStayDays));
        }
        
        // Validate maximum stay duration
        if (stayDays > maxStayDays) {
            throw new IllegalArgumentException(String.format("Maximum stay duration is %d days", maxStayDays));
        }
        
        // Validate based on booking type
        switch (bookingType.toUpperCase()) {
            case "IMMEDIATE":
                // Immediate bookings must be for today or future
                if (checkIn.isBefore(today)) {
                    throw new IllegalArgumentException("Immediate bookings cannot be for past dates");
                }
                break;
                
            case "ADVANCE":
                // Advance bookings must be for future dates
                if (checkIn.isBefore(today)) {
                    throw new IllegalArgumentException("Advance bookings cannot be for past dates");
                }
                
                // Validate advance booking time limit
                validateAdvanceBookingLimit(checkIn, maxAdvanceDays);
                break;
                
            case "RESERVATION":
                // Reservations can be for future dates
                if (checkIn.isBefore(today)) {
                    throw new IllegalArgumentException("Reservations cannot be for past dates");
                }
                break;
                
            default:
                // For general validation, ensure dates are not in the past
                if (checkIn.isBefore(today)) {
                    throw new IllegalArgumentException("Check-in date cannot be in the past");
                }
                break;
        }
        
        logger.debug("Date range validation passed");
    }
    
    @Override
    public void validateStatusTransition(BookingStatus currentStatus, BookingStatus newStatus) {
        logger.debug("Validating status transition: {} -> {}", currentStatus, newStatus);
        
        if (currentStatus == null || newStatus == null) {
            throw new BusinessException("Current and new status cannot be null");
        }
        
        // Same status is always valid
        if (currentStatus == newStatus) {
            return;
        }
        
        // Check if transition is allowed
        Set<BookingStatus> allowedTransitions = VALID_STATUS_TRANSITIONS.get(currentStatus);
        if (allowedTransitions == null) {
            logger.warn("No transition rules defined for booking status: {}", currentStatus);
            throw new BusinessException("Invalid current booking status: " + currentStatus);
        }
        
        if (!allowedTransitions.contains(newStatus)) {
            logger.warn("Invalid status transition: {} -> {} (allowed: {})", 
                        currentStatus, newStatus, allowedTransitions);
            throw new BusinessException(String.format("Invalid status transition from %s to %s", 
                                                    currentStatus, newStatus));
        }
        
        logger.debug("Status transition validation passed");
    }
    
    @Override
    public void validateCancellation(Booking booking) {
        logger.debug("Validating cancellation for booking: {}", booking.getId());
        
        if (booking == null) {
            throw new BusinessException("Booking cannot be null");
        }
        
        // Check if booking is in a cancellable state
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Booking is already cancelled");
        }
        
        if (booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new BusinessException("Cannot cancel a completed booking");
        }
        
        // Allow cancellation requests even if check-in has started (for staff approval)
        // The business logic will determine if cancellation is appropriate
        logger.debug("Cancellation validation passed");
    }
    
    @Override
    public void validateConfirmation(Booking booking) {
        logger.debug("Validating confirmation for booking: {}", booking.getId());
        
        if (booking == null) {
            throw new BusinessException("Booking cannot be null");
        }
        
        // Only pending bookings can be confirmed
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Only pending bookings can be confirmed");
        }
        
        // Check if check-in date is still valid
        LocalDate today = LocalDate.now();
        if (booking.getCheckInDate().isBefore(today)) {
            throw new BusinessException("Cannot confirm a booking with past check-in date");
        }
        
        logger.debug("Confirmation validation passed");
    }
    
    @Override
    public void validateGuestCount(Long roomId, int guestCount) {
        logger.debug("Validating guest count: {} for room: {}", guestCount, roomId);
        
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }
        
        if (guestCount <= 0) {
            throw new IllegalArgumentException("Guest count must be greater than 0");
        }
        
        // Fetch room to check capacity
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new BusinessException("Room not found with id: " + roomId));
        
        if (guestCount > room.getMaxGuests()) {
            throw new BusinessException(String.format("Guest count %d exceeds room capacity %d", 
                                                    guestCount, room.getMaxGuests()));
        }
        
        logger.debug("Guest count validation passed");
    }
    
    @Override
    public void validateBusinessHours(LocalDateTime checkInDateTime) {
        logger.debug("Validating business hours for check-in: {}", checkInDateTime);
        
        if (checkInDateTime == null) {
            throw new IllegalArgumentException("Check-in date and time cannot be null");
        }
        
        LocalTime checkInTime = checkInDateTime.toLocalTime();
        
        // Check if check-in time is within business hours
        if (checkInTime.isBefore(BUSINESS_HOURS_START) || checkInTime.isAfter(BUSINESS_HOURS_END)) {
            throw new BusinessException(String.format("Check-in time must be between %s and %s", 
                                                    BUSINESS_HOURS_START, BUSINESS_HOURS_END));
        }
        
        logger.debug("Business hours validation passed");
    }
    
    @Override
    public void validateAdvanceBookingLimit(LocalDate checkInDate, int maxAdvanceDays) {
        logger.debug("Validating advance booking limit: {} days for check-in: {}", maxAdvanceDays, checkInDate);
        
        if (checkInDate == null) {
            throw new IllegalArgumentException("Check-in date cannot be null");
        }
        
        if (maxAdvanceDays <= 0) {
            throw new IllegalArgumentException("Maximum advance days must be positive");
        }
        
        LocalDate today = LocalDate.now();
        LocalDate maxAdvanceDate = today.plusDays(maxAdvanceDays);
        
        if (checkInDate.isAfter(maxAdvanceDate)) {
            throw new BusinessException(String.format("Advance bookings cannot be made more than %d days in advance", 
                                                    maxAdvanceDays));
        }
        
        logger.debug("Advance booking limit validation passed");
    }
    
    /**
     * Validate phone number format.
     * 
     * @param phone The phone number to validate
     * @throws IllegalArgumentException if phone number format is invalid
     */
    private void validatePhoneNumber(String phone) {
        // Basic phone number validation (can be enhanced with more sophisticated regex)
        if (!phone.matches("^[+]?[0-9\\-\\s()]{7,20}$")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
    }
}
