package com.yakrooms.be.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
public class ContactController {

    // Contact form submission - Public access
    @PostMapping("/getIntouch")
    public ResponseEntity<Map<String, Object>> submitContactForm(@RequestBody Map<String, String> contactForm) {
        try {
            // TODO: Implement actual contact form processing logic
            // This could include sending emails, storing in database, etc.
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thank you for your message. We'll get back to you soon!");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to submit contact form. Please try again.");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
