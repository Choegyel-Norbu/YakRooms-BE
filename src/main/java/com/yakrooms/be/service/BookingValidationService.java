package com.yakrooms.be.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.yakrooms.be.dto.request.BookingRequest;
import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.enums.BookingStatus;

/**
 * Service for comprehensive booking validation including dates, business rules, and constraints.
 * This service centralizes all validation logic to ensure consistency across the system.
 * 
 * @author YakRooms Team
 * @version 1.0
 */
public interface BookingValidationService {
    
    /**
     * Validate a complete booking request.
     * This method performs comprehensive validation of all booking parameters.
     * 
     * @param request The booking request to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validateBookingRequest(BookingRequest request);
    
    /**
     * Validate date range for a booking.
     * This method ensures dates are valid and follow business rules.
     * 
     * @param checkIn The check-in date
     * @param checkOut The check-out date
     * @param bookingType The type of booking (IMMEDIATE, ADVANCE, RESERVATION)
     * @throws IllegalArgumentException if date validation fails
     */
    void validateDateRange(LocalDate checkIn, LocalDate checkOut, String bookingType);
    
    /**
     * Validate that a booking status transition is allowed.
     * This method enforces business rules for status changes.
     * 
     * @param currentStatus The current booking status
     * @param newStatus The desired new status
     * @throws BusinessException if transition is not allowed
     */
    void validateStatusTransition(BookingStatus currentStatus, BookingStatus newStatus);
    
    /**
     * Validate that a booking can be cancelled.
     * This method checks business rules for cancellation.
     * 
     * @param booking The booking to validate
     * @throws BusinessException if cancellation is not allowed
     */
    void validateCancellation(Booking booking);
    
    /**
     * Validate that a booking can be confirmed.
     * This method checks business rules for confirmation.
     * 
     * @param booking The booking to validate
     * @throws BusinessException if confirmation is not allowed
     */
    void validateConfirmation(Booking booking);
    
    /**
     * Validate guest count against room capacity.
     * This method ensures the room can accommodate the requested number of guests.
     * 
     * @param roomId The room ID
     * @param guestCount The number of guests
     * @throws BusinessException if guest count exceeds room capacity
     */
    void validateGuestCount(Long roomId, int guestCount);
    
    /**
     * Validate business hours for check-in.
     * This method ensures check-in times are within acceptable business hours.
     * 
     * @param checkInDateTime The check-in date and time
     * @throws BusinessException if check-in is outside business hours
     */
    void validateBusinessHours(LocalDateTime checkInDateTime);
    
    /**
     * Validate advance booking time limits.
     * This method ensures advance bookings are not too far in the future.
     * 
     * @param checkInDate The check-in date
     * @param maxAdvanceDays The maximum number of days in advance
     * @throws BusinessException if booking is too far in advance
     */
    void validateAdvanceBookingLimit(LocalDate checkInDate, int maxAdvanceDays);
}
