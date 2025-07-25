package com.yakrooms.be.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yakrooms.be.dto.NotificationMessage;

@Service
public class NotificationService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ObjectMapper objectMapper; // Add this
    
    public void notifyUser(String userId, NotificationMessage payload) {
        String destination = "/topic/notifications/" + userId;
        System.out.println("=== SENDING WEBSOCKET MESSAGE ===");
        System.out.println("Destination: " + destination);
        System.out.println("Payload: " + payload);
        
        try {
            // Convert to JSON string first to ensure proper serialization
//            String jsonPayload = objectMapper.writeValueAsString(payload);
//            System.out.println("JSON Payload: " + jsonPayload);
            
            messagingTemplate.convertAndSend(destination, payload);  // Pass object directly

            System.out.println("Message sent successfully!");
        } catch (Exception e) {
            System.err.println("Failed to send WebSocket message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
