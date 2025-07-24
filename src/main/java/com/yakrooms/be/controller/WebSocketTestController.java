package com.yakrooms.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class WebSocketTestController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Test endpoint to manually trigger a WebSocket message
    @GetMapping("/test/websocket/{hotelId}")
    public Map<String, String> testWebSocket(@PathVariable Long hotelId) {
        try {
            // Create a test message
            Map<String, Object> testMessage = new HashMap<>();
            testMessage.put("message", "Test WebSocket connection");
            testMessage.put("hotelId", hotelId);
            testMessage.put("timestamp", System.currentTimeMillis());
            
            // Send test message to the topic
            messagingTemplate.convertAndSend("/topic/rooms/" + hotelId, testMessage);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Test message sent to /topic/rooms/" + hotelId);
            return response;
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return response;
        }
    }
}

@Controller
class WebSocketMessageController {
    
    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Map<String, String> greeting(Map<String, String> message) throws Exception {
        Thread.sleep(1000); // simulated delay
        Map<String, String> response = new HashMap<>();
        response.put("content", "Hello, " + message.get("name") + "!");
        return response;
    }
}