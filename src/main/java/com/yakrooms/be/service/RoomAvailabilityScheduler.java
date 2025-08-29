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
 * This service has been simplified after removing the RoomAvailabilityService.
 * 
 * @author YakRooms Team
 * @version 2.0
 */
@Service
public class RoomAvailabilityScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(RoomAvailabilityScheduler.class);
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Scheduled task to process room availability updates.
     * This method now logs that the service has been removed and simplified.
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void processRoomAvailabilityUpdates() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("Room availability scheduler triggered at: {}", now.format(formatter));
        logger.info("RoomAvailabilityService has been removed. Room availability is now managed directly by booking services.");
        
        // No-op: Room availability is now managed directly by booking services
        // This maintains the scheduled job structure while removing the dependency
    }
    
    /**
     * Manual trigger for room availability updates.
     * This method now logs that the service has been removed and simplified.
     */
    public void manualRoomAvailabilityUpdate() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("Manual room availability update triggered at: {}", now.format(formatter));
        logger.info("RoomAvailabilityService has been removed. Room availability is now managed directly by booking services.");
        
        // No-op: Room availability is now managed directly by booking services
        // This maintains the manual trigger structure while removing the dependency
    }
}
