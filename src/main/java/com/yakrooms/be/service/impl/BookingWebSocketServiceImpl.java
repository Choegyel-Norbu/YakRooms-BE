package com.yakrooms.be.service.impl;

import com.yakrooms.be.dto.BookingChangeEvent;
import com.yakrooms.be.service.BookingWebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class BookingWebSocketServiceImpl implements BookingWebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(BookingWebSocketServiceImpl.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastBookingStatusChange(BookingChangeEvent event) {
        try {
            // Broadcast to general booking topic
            messagingTemplate.convertAndSend("/topic/bookings", event);
           
            
            // Broadcast to specific user topic
            if (event.getUserId() != null) {
                broadcastToUser(event.getUserId(), event);
            }
            
            logger.info("Broadcasted booking change event: {}", event);
        } catch (Exception e) {
            logger.error("Failed to broadcast booking change event: {}", event, e);
        }
    }

    @Override
    public void broadcastToHotel(Long hotelId, BookingChangeEvent event) {
        try {
            String destination = "/topic/hotels/" + hotelId + "/bookings";
            messagingTemplate.convertAndSend(destination, event);
            logger.info("Broadcasted booking event to hotel {}: {}", hotelId, event.getEventType());
        } catch (Exception e) {
            logger.error("Failed to broadcast booking event to hotel {}: {}", hotelId, e.getMessage());
        }
    }

    @Override
    public void broadcastToUser(Long userId, BookingChangeEvent event) {
        try {
            String destination = "/queue/users/" + userId + "/bookings";
            messagingTemplate.convertAndSend(destination, event);
            logger.info("Broadcasted booking event to user {}: {}", userId, event.getEventType());
        } catch (Exception e) {
            logger.error("Failed to broadcast booking event to user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void notifyBookingUpdates(Long hotelId, String message) {
        try {
            String destination = "/topic/hotels/" + hotelId + "/updates";
            messagingTemplate.convertAndSend(destination, message);
            logger.info("Broadcasted generic update to hotel {}: {}", hotelId, message);
        } catch (Exception e) {
            logger.error("Failed to broadcast generic update to hotel {}: {}", hotelId, e.getMessage());
        }
    }
} 