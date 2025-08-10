package com.yakrooms.be.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/websocket")
public class WebSocketTestController {

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	// Test endpoint to manually trigger a WebSocket message
	@GetMapping("/test")
	public Map<String, String> testWebSocket() {
		Map<String, String> response = new HashMap<>();
		response.put("message", "WebSocket test endpoint called");
		
		// Send a test message to a specific destination
		messagingTemplate.convertAndSend("/topic/test", "Hello from WebSocket Test Controller!");
		
		return response;
	}
}