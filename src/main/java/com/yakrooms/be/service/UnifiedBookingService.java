package com.yakrooms.be.service;

import com.yakrooms.be.dto.request.BookingRequest;
import com.yakrooms.be.dto.request.BookingExtensionRequest;
import com.yakrooms.be.dto.response.BookingResponse;
import com.yakrooms.be.dto.response.BookingExtensionResponse;

/**
 * UnifiedBookingService interface that handles ONLY booking creation with pessimistic locking.
 * This service is focused on preventing race conditions during concurrent booking creation.
 * All other booking operations are handled by BookingService.
 * 
 * @author YakRooms Team
 * @version 2.0
 */
public interface UnifiedBookingService {
    
    /**
     * Create a new booking with pessimistic locking to prevent race conditions.
     * This method ensures atomicity and prevents concurrent booking conflicts.
     * 
     * @param request The booking request
     * @return The created booking response
     */
    BookingResponse createBooking(BookingRequest request);
    
    /**
     * Extend an existing booking's stay duration.
     * This method allows guests to extend their stay beyond the original check-out date.
     * 
     * @param bookingId The ID of the existing booking to extend
     * @param request The extension request containing new check-out date and optional updates
     * @return The extension response with updated booking details and additional cost
     */
    BookingExtensionResponse extendBooking(Long bookingId, BookingExtensionRequest request);
    
    /**
     * Check room availability for any date range.
     * This method provides unified availability checking for all booking types.
     * 
     * @param roomId The room ID
     * @param checkIn The check-in date
     * @param checkOut The check-out date
     * @return true if room is available, false otherwise
     */
    boolean checkRoomAvailability(Long roomId, java.time.LocalDate checkIn, java.time.LocalDate checkOut);
    
    /**
     * Check room availability with specific check-in and check-out times.
     * This method provides more granular availability checking including time-based conflicts.
     * 
     * @param roomId The room ID
     * @param checkIn The check-in date
     * @param checkInTime The check-in time
     * @param checkOut The check-out date
     * @param checkOutTime The check-out time
     * @return true if room is available, false otherwise
     */
    boolean checkRoomAvailabilityWithTimes(Long roomId, java.time.LocalDate checkIn, java.time.LocalTime checkInTime, 
                                         java.time.LocalDate checkOut, java.time.LocalTime checkOutTime);
}
