package com.yakrooms.be.dto;

import com.yakrooms.be.model.enums.BookingStatus;
import java.time.LocalDateTime;

public class BookingChangeEvent {
    private Long bookingId;
    private Long hotelId;
    private Long userId;
    private BookingStatus oldStatus;
    private BookingStatus newStatus;
    private String eventType;
    private LocalDateTime timestamp;
    private String message;

    // Default constructor
    public BookingChangeEvent() {
        this.timestamp = LocalDateTime.now();
    }

    // Constructor with all fields
    public BookingChangeEvent(Long bookingId, Long hotelId, Long userId, 
                            BookingStatus oldStatus, BookingStatus newStatus, 
                            String eventType, String message) {
        this.bookingId = bookingId;
        this.hotelId = hotelId;
        this.userId = userId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.eventType = eventType;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BookingStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(BookingStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public BookingStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(BookingStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "BookingChangeEvent{" +
                "bookingId=" + bookingId +
                ", hotelId=" + hotelId +
                ", userId=" + userId +
                ", oldStatus=" + oldStatus +
                ", newStatus=" + newStatus +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                '}';
    }
} 