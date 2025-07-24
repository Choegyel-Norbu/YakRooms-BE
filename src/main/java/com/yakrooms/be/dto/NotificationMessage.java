package com.yakrooms.be.dto;

import java.time.Instant;

public class NotificationMessage {
	
	private String title;
    private String message;
    private String type; // e.g., "BOOKING", "CANCELLATION", "SYSTEM_ALERT"
    private Instant timestamp;

    // Constructors, Getters, and Setters

    public NotificationMessage() {
        this.timestamp = Instant.now();
    }

    public NotificationMessage(String title, String message, String type) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = Instant.now();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
