package com.yakrooms.be.service;

import com.yakrooms.be.dto.request.BookingRequest;
import com.yakrooms.be.dto.response.BookingResponse;

public interface UnifiedBookingService {
    
    BookingResponse createBooking(BookingRequest request);
    

    
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
    
    /**
     * Confirm a pending booking.
     * This method transitions a booking from PENDING to CONFIRMED status.
     * 
     * @param bookingId The booking ID to confirm
     * @return The updated booking response
     */
    BookingResponse confirmBooking(Long bookingId);
    
    /**
     * Cancel a booking.
     * This method handles cancellation with proper room availability restoration.
     * 
     * @param bookingId The booking ID to cancel
     * @param userId The user ID requesting cancellation
     */
    void cancelBooking(Long bookingId, Long userId);
    
    /**
     * Update booking status with proper validation.
     * This method ensures status transitions follow business rules.
     * 
     * @param bookingId The booking ID
     * @param newStatus The new status
     * @return true if update was successful
     */
    boolean updateBookingStatus(Long bookingId, String newStatus);
}
