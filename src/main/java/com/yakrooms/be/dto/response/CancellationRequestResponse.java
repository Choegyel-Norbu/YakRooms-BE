package com.yakrooms.be.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Simplified DTO for cancellation request response containing essential booking details
 */
public class CancellationRequestResponse {
    
    private Long bookingId;
    private String guestName;
    private String phone;
    private String userName;
    private String status;
    private LocalDateTime bookingCreatedAt;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    
    // Constructors
    public CancellationRequestResponse() {}
    
    // Getters and Setters
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getBookingCreatedAt() { return bookingCreatedAt; }
    public void setBookingCreatedAt(LocalDateTime bookingCreatedAt) { this.bookingCreatedAt = bookingCreatedAt; }
    
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
}
