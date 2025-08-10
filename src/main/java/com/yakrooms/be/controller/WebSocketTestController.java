package com.yakrooms.be.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RestController
@RequestMapping("/api/websocket")
public class WebSocketTestController {

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private TemplateEngine templateEngine;

	// Test endpoint to manually trigger a WebSocket message
	@GetMapping("/test")
	public Map<String, String> testWebSocket() {
		Map<String, String> response = new HashMap<>();
		response.put("message", "WebSocket test endpoint called");
		
		// Send a test message to a specific destination
		messagingTemplate.convertAndSend("/topic/test", "Hello from WebSocket Test Controller!");
		
		return response;
	}

	@GetMapping("/test-template")
	public ResponseEntity<String> testTemplate() {
		try {
			// Check if template file exists
			ClassPathResource resource = new ClassPathResource("templates/booking-passcode.html");
			boolean exists = resource.exists();
			
			if (!exists) {
				return ResponseEntity.ok("Template file does not exist in classpath");
			}
			
			// Try to process template
			Context context = new Context();
			context.setVariable("test", "Template Test");
			String result = templateEngine.process("booking-passcode", context);
			
			return ResponseEntity.ok("Template processed successfully. Length: " + result.length());
		} catch (Exception e) {
			return ResponseEntity.status(500)
					.body("Template processing failed: " + e.getMessage());
		}
	}
}