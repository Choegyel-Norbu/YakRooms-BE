package com.yakrooms.be.dto;

import java.time.Instant;

public class NotificationMessage {
	
	private String title;
    private String message;
    private String type; // e.g., "BOOKING", "CANCELLATION", "SYSTEM_ALERT"
   
    // Constructors, Getters, and Setters

    public NotificationMessage() {
    }

    public NotificationMessage(String title, String message, String type) {
        this.title = title;
        this.message = message;
        this.type = type;
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
    
    @Override
    public String toString() {
        return "NotificationMessage{" +
                "title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
