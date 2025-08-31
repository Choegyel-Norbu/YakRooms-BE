package com.yakrooms.be.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduler for room availability updates.
 * This service runs daily at noon to update room availability based on check-in/check-out dates.
 * 
 * @author YakRooms Team
 * @version 3.0
 */
@Service
public class RoomAvailabilityScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(RoomAvailabilityScheduler.class);
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final RoomAvailabilityService roomAvailabilityService;
    
    @Autowired
    public RoomAvailabilityScheduler(RoomAvailabilityService roomAvailabilityService) {
        this.roomAvailabilityService = roomAvailabilityService;
    }
    
    /**
     * Scheduled task to process room availability updates daily at noon (12:00 PM).
     * This job handles:
     * - Rooms becoming available (checkout completed today)
     * - Rooms becoming unavailable (checkin starting today)
     */
    @Scheduled(cron = "0 0 12 * * ?") // Every day at 12:00 PM
    public void processDailyRoomAvailabilityUpdates() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("Daily room availability scheduler triggered at: {}", now.format(formatter));
        
        try {
            int updatedRooms = roomAvailabilityService.processDailyRoomAvailabilityUpdates();
            logger.info("Daily room availability update completed successfully. {} rooms updated.", updatedRooms);
        } catch (Exception e) {
            logger.error("Failed to process daily room availability updates", e);
        }
    }
    
    /**
     * Manual trigger for room availability updates.
     * Useful for testing or immediate updates.
     */
    public void manualRoomAvailabilityUpdate() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("Manual room availability update triggered at: {}", now.format(formatter));
        
        try {
            int updatedRooms = roomAvailabilityService.processDailyRoomAvailabilityUpdates();
            logger.info("Manual room availability update completed successfully. {} rooms updated.", updatedRooms);
        } catch (Exception e) {
            logger.error("Failed to process manual room availability updates", e);
        }
    }
}
 