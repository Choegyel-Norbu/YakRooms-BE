package com.yakrooms.be.service;

/**
 * Service for managing room availability updates.
 * This service centralizes room availability logic for both scheduled jobs and booking workflows.
 * 
 * @author YakRooms Team
 * @version 3.0
 */
public interface RoomAvailabilityService {
    
    /**
     * Process daily room availability updates at noon.
     * This method should be called by the scheduler to handle:
     * - Rooms becoming available (checkout completed)
     * - Rooms becoming unavailable (checkin starting)
     * 
     * @return The number of rooms that had their availability updated
     */
    int processDailyRoomAvailabilityUpdates();
    
    /**
     * Update room availability when a booking is created.
     * 
     * @param roomId The room ID
     * @param checkInDate The check-in date
     * @param checkInTime The check-in time
     * @return true if the update was successful
     */
    boolean updateRoomAvailabilityForNewBooking(Long roomId, java.time.LocalDate checkInDate, java.time.LocalTime checkInTime);
    
    /**
     * Update room availability when a booking is cancelled.
     * 
     * @param roomId The room ID
     * @param checkInDate The check-in date
     * @return true if the update was successful
     */
    boolean updateRoomAvailabilityForCancelledBooking(Long roomId, java.time.LocalDate checkInDate);
    
    /**
     * Update room availability when a booking is confirmed.
     * 
     * @param roomId The room ID
     * @param checkInDate The check-in date
     * @return true if the update was successful
     */
    boolean updateRoomAvailabilityForConfirmedBooking(Long roomId, java.time.LocalDate checkInDate);
    
    /**
     * Update room availability when a guest checks in.
     * 
     * @param roomId The room ID
     * @return true if the update was successful
     */
    boolean updateRoomAvailabilityForCheckIn(Long roomId);
    
    /**
     * Update room availability when a guest checks out.
     * 
     * @param roomId The room ID
     * @return true if the update was successful
     */
    boolean updateRoomAvailabilityForCheckOut(Long roomId);
}
