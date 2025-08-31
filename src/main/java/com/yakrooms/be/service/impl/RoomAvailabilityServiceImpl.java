package com.yakrooms.be.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.model.enums.BookingStatus;
import com.yakrooms.be.repository.BookingRepository;
import com.yakrooms.be.repository.RoomRepository;
import com.yakrooms.be.service.RoomAvailabilityService;

/**
 * Implementation of RoomAvailabilityService.
 * This service provides efficient, transactional room availability management
 * with bulk operations and comprehensive logging.
 * 
 * NOTE: Only new bookings with check-in date equal to today will update room availability.
 * All other availability updates are handled by the job scheduler (cron job).
 * 
 * @author YakRooms Team
 * @version 3.0
 */
@Service
public class RoomAvailabilityServiceImpl implements RoomAvailabilityService {
    
    private static final Logger logger = LoggerFactory.getLogger(RoomAvailabilityServiceImpl.class);
    
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    
    @Autowired
    public RoomAvailabilityServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }
    
    @Override
    @Transactional
    public int processDailyRoomAvailabilityUpdates() {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        
        logger.info("Starting daily room availability update process for date: {}", today);
        
        int totalUpdates = 0;
        
        try {
            // Step 1: Find rooms that should become available (checkout completed today)
            int availableUpdates = processCheckoutCompletions(today);
            totalUpdates += availableUpdates;
            
            // Step 2: Find rooms that should become unavailable (checkin starting today)
            int unavailableUpdates = processCheckinStarts(today, currentTime);
            totalUpdates += unavailableUpdates;
            
            logger.info("Daily room availability update completed. Total rooms updated: {}", totalUpdates);
            
        } catch (Exception e) {
            logger.error("Failed to process daily room availability updates for date: {}", today, e);
            throw new RuntimeException("Failed to process daily room availability updates", e);
        }
        
        return totalUpdates;
    }
    
    /**
     * Process rooms that should become available due to checkout completion.
     * 
     * @param today The date to process
     * @return Number of rooms updated
     */
    private int processCheckoutCompletions(LocalDate today) {
        logger.debug("Processing checkout completions for date: {}", today);
        
        // Find all bookings where check_out_date = today and status in (CONFIRMED, CHECKED_IN)
        List<Long> roomIdsToMakeAvailable = bookingRepository
            .findRoomIdsByCheckoutDateAndStatuses(today, Set.of(BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN));
        
        if (roomIdsToMakeAvailable.isEmpty()) {
            logger.debug("No rooms need to be made available for checkout date: {}", today);
            return 0;
        }
        
        logger.info("Found {} rooms to make available due to checkout completion on {}", 
                   roomIdsToMakeAvailable.size(), today);
        
        // Bulk update room availability to true
        int updatedCount = roomRepository.bulkUpdateRoomAvailability(roomIdsToMakeAvailable, true);
        
        logger.info("Successfully made {} rooms available due to checkout completion on {}", 
                   updatedCount, today);
        
        return updatedCount;
    }
    
    /**
     * Process rooms that should become unavailable due to checkin starting.
     * 
     * @param today The date to process
     * @param currentTime The current time
     * @return Number of rooms updated
     */
    private int processCheckinStarts(LocalDate today, LocalTime currentTime) {
        logger.debug("Processing checkin starts for date: {} at time: {}", today, currentTime);
        
        // Find all bookings where check_in_date = today and status in (CONFIRMED, PENDING)
        List<Long> roomIdsToMakeUnavailable = bookingRepository
            .findRoomIdsByCheckinDateAndStatuses(today, Set.of(BookingStatus.CONFIRMED, BookingStatus.PENDING));
        
        if (roomIdsToMakeUnavailable.isEmpty()) {
            logger.debug("No rooms need to be made unavailable for checkin date: {}", today);
            return 0;
        }
        
        logger.info("Found {} rooms to make unavailable due to checkin starting on {}", 
                   roomIdsToMakeUnavailable.size(), today);
        
        // Bulk update room availability to false
        int updatedCount = roomRepository.bulkUpdateRoomAvailability(roomIdsToMakeUnavailable, false);
        
        logger.info("Successfully made {} rooms unavailable due to checkin starting on {}", 
                   updatedCount, today);
        
        return updatedCount;
    }
    
    @Override
    @Transactional
    public boolean updateRoomAvailabilityForNewBooking(Long roomId, LocalDate checkInDate, LocalTime checkInTime) {
        try {
            LocalDate today = LocalDate.now();
            
            // Only update room availability if check-in is today
            // This ensures immediate availability update for same-day check-ins
            if (checkInDate.isEqual(today)) {
                logger.info("New booking for room {} with check-in today - making room unavailable", roomId);
                return updateRoomAvailabilityAtomically(roomId, false);
            }
            
            // For future check-ins, room availability is managed by the job scheduler
            logger.debug("New booking for room {} with future check-in date {} - no immediate availability update needed", 
                        roomId, checkInDate);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to update room {} availability for new booking: {}", roomId, e.getMessage());
            return false;
        }
    }
    
    // COMMENTED OUT: These methods are now handled by the job scheduler (cron job)
    // to avoid duplicate availability updates and ensure consistency
    
    @Override
    @Transactional
    public boolean updateRoomAvailabilityForCancelledBooking(Long roomId, LocalDate checkInDate) {
        try {
            LocalDate today = LocalDate.now();
            
            // If the cancelled booking was for today, make room available immediately
            // This ensures cancelled same-day bookings don't leave rooms unavailable
            if (checkInDate.isEqual(today)) {
                logger.info("Cancelled booking for room {} with check-in today - making room available immediately", roomId);
                return updateRoomAvailabilityAtomically(roomId, true);
            }
            
            // For future cancellations, room availability is managed by the job scheduler
            logger.debug("Cancelled booking for room {} with future check-in date {} - no immediate availability update needed", 
                        roomId, checkInDate);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to update room {} availability for cancelled booking: {}", roomId, e.getMessage());
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean updateRoomAvailabilityForConfirmedBooking(Long roomId, LocalDate checkInDate) {
        // COMMENTED OUT: This logic is now handled by the job scheduler (cron job)
        // to avoid duplicate availability updates and ensure consistency
        logger.debug("Room availability update for confirmed booking {} handled by job scheduler", roomId);
        return true; // No-op - handled by scheduler
    }
    
    @Override
    @Transactional
    public boolean updateRoomAvailabilityForCheckIn(Long roomId) {
        // COMMENTED OUT: This logic is now handled by the job scheduler (cron job)
        // to avoid duplicate availability updates and ensure consistency
        logger.debug("Room availability update for check-in {} handled by job scheduler", roomId);
        return true; // No-op - handled by scheduler
    }
    
    @Override
    @Transactional
    public boolean updateRoomAvailabilityForCheckOut(Long roomId) {
        // COMMENTED OUT: This logic is now handled by the job scheduler (cron job)
        // to avoid duplicate availability updates and ensure consistency
        logger.debug("Room availability update for check-out {} handled by job scheduler", roomId);
        return true; // No-op - handled by scheduler
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
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
            
            // Only update if the current availability is different
            if (room.isAvailable() != available) {
                room.setAvailable(available);
                roomRepository.save(room);
                logger.info("Updated room {} availability from {} to {}", roomId, !available, available);
            } else {
                logger.debug("Room {} availability already set to {}, no update needed", roomId, available);
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Failed to update room {} availability to {}: {}", roomId, available, e.getMessage());
            return false;
        }
    }
}
