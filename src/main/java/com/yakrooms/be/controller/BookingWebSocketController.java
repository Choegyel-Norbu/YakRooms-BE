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

    // Test endpoint to manually trigger a booking status change event
    @PostMapping("/test/status-change")
    public Map<String, Object> testBookingStatusChange(
            @RequestParam Long bookingId,
            @RequestParam Long hotelId,
            @RequestParam Long userId,
            @RequestParam String oldStatus,
            @RequestParam String newStatus) {
        
        try {
            BookingStatus oldBookingStatus = BookingStatus.valueOf(oldStatus.toUpperCase());
            BookingStatus newBookingStatus = BookingStatus.valueOf(newStatus.toUpperCase());
            
            BookingChangeEvent event = new BookingChangeEvent(
                bookingId,
                hotelId,
                userId,
                oldBookingStatus,
                newBookingStatus,
                "BOOKING_STATUS_CHANGE",
                String.format("Test: Booking status changed from %s to %s", oldStatus, newStatus)
            );
            
            // Broadcast the test event
            bookingWebSocketService.broadcastBookingStatusChange(event);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Test booking status change event sent");
            response.put("event", event);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Test booking status change event sent: {}", event);
            return response;
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to send test event: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            logger.error("Failed to send test booking status change event", e);
            return response;
        }
    }

    // Test endpoint to send a check-out event specifically
    @PostMapping("/test/checkout")
    public Map<String, Object> testCheckoutEvent(
            @RequestParam Long bookingId,
            @RequestParam Long hotelId,
            @RequestParam Long userId) {
        
        try {
            BookingChangeEvent event = new BookingChangeEvent(
                bookingId,
                hotelId,
                userId,
                BookingStatus.CHECKED_IN,
                BookingStatus.CHECKED_OUT,
                "BOOKING_CHECKOUT",
                "Guest has checked out from the hotel"
            );
            
            // Broadcast the check-out event
            bookingWebSocketService.broadcastBookingStatusChange(event);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Check-out event sent successfully");
            response.put("event", event);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Test check-out event sent: {}", event);
            return response;
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to send check-out event: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            logger.error("Failed to send test check-out event", e);
            return response;
        }
    }

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