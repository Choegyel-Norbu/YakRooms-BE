package com.yakrooms.be.controller;

import com.yakrooms.be.dto.BookingChangeEvent;
import com.yakrooms.be.model.enums.BookingStatus;
import com.yakrooms.be.service.BookingWebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/websocket/bookings")
public class BookingWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(BookingWebSocketController.class);

    @Autowired
    private BookingWebSocketService bookingWebSocketService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    // WebSocket message handler for booking-related messages
    @MessageMapping("/bookings/status")
    @SendTo("/topic/bookings/status-updates")
    public Map<String, Object> handleBookingStatusMessage(Map<String, Object> message) {
        logger.info("Received booking status message: {}", message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", "BOOKING_STATUS_ACKNOWLEDGMENT");
        response.put("message", "Booking status message received");
        response.put("originalMessage", message);
        response.put("timestamp", LocalDateTime.now());
        
        return response;
    }

    // Get WebSocket connection status
    @GetMapping("/status")
    public Map<String, Object> getWebSocketStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "active");
        status.put("message", "Booking WebSocket service is running");
        status.put("timestamp", LocalDateTime.now());
        status.put("endpoints", Map.of(
            "general", "/topic/bookings",
            "hotel_specific", "/topic/hotels/{hotelId}/bookings",
            "user_specific", "/queue/users/{userId}/bookings"
        ));
        
        return status;
    }
} 