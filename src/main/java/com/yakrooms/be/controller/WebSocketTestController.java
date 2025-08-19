package com.yakrooms.be.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/websocket")
public class WebSocketTestController {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketTestController.class);

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	// Test endpoint to manually trigger a WebSocket message
	@GetMapping("/test")
	public Map<String, String> testWebSocket() {
		logger.info("WebSocket test endpoint called");
		Map<String, String> response = new HashMap<>();
		response.put("message", "WebSocket test endpoint called");
		
		// Send a test message to a specific destination
		messagingTemplate.convertAndSend("/topic/test", "Hello from WebSocket Test Controller!");
		logger.info("Sent test message to /topic/test");
		
		return response;
	}
	
	// WebSocket message handler for test messages
	@MessageMapping("/test")
	@SendTo("/topic/test")
	public Map<String, Object> handleTestMessage(Map<String, Object> message) {
		logger.info("Received test message: {}", message);
		
		Map<String, Object> response = new HashMap<>();
		response.put("type", "TEST_MESSAGE_ACKNOWLEDGMENT");
		response.put("message", "Test message received and acknowledged");
		response.put("originalMessage", message);
		response.put("timestamp", System.currentTimeMillis());
		response.put("serverResponse", "Hello from YakRooms WebSocket server!");
		
		logger.info("Sending test response: {}", response);
		return response;
	}
}