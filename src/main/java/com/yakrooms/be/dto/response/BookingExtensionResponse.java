package com.yakrooms.be.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Response DTO for booking extension operations.
 * Contains the updated booking information and extension details.
 * 
 * @author YakRooms Team
 * @version 1.0
 */
public class BookingExtensionResponse {
    
    private Long bookingId;
    private LocalDate originalCheckOutDate;
    private LocalDate newCheckOutDate;
    private int additionalDays;
    private BigDecimal originalPrice;
    private BigDecimal additionalCost;
    private BigDecimal newTotalPrice;
    private String message;
    private boolean success;
    
    // Constructors
    public BookingExtensionResponse() {}
    
    public BookingExtensionResponse(Long bookingId, LocalDate originalCheckOutDate, 
                                   LocalDate newCheckOutDate, BigDecimal originalPrice, 
                                   BigDecimal additionalCost, BigDecimal newTotalPrice) {
        this.bookingId = bookingId;
        this.originalCheckOutDate = originalCheckOutDate;
        this.newCheckOutDate = newCheckOutDate;
        this.originalPrice = originalPrice;
        this.additionalCost = additionalCost;
        this.newTotalPrice = newTotalPrice;
        this.additionalDays = (int) ChronoUnit.DAYS.between(originalCheckOutDate, newCheckOutDate);
        this.success = true;
        this.message = "Booking extended successfully";
    }
    
    public BookingExtensionResponse(Long bookingId, String message, boolean success) {
        this.bookingId = bookingId;
        this.message = message;
        this.success = success;
    }
    
    // Getters and Setters
    public Long getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
    
    public LocalDate getOriginalCheckOutDate() {
        return originalCheckOutDate;
    }
    
    public void setOriginalCheckOutDate(LocalDate originalCheckOutDate) {
        this.originalCheckOutDate = originalCheckOutDate;
    }
    
    public LocalDate getNewCheckOutDate() {
        return newCheckOutDate;
    }
    
    public void setNewCheckOutDate(LocalDate newCheckOutDate) {
        this.newCheckOutDate = newCheckOutDate;
    }
    
    public int getAdditionalDays() {
        return additionalDays;
    }
    
    public void setAdditionalDays(int additionalDays) {
        this.additionalDays = additionalDays;
    }
    
    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }
    
    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }
    
    public BigDecimal getAdditionalCost() {
        return additionalCost;
    }
    
    public void setAdditionalCost(BigDecimal additionalCost) {
        this.additionalCost = additionalCost;
    }
    
    public BigDecimal getNewTotalPrice() {
        return newTotalPrice;
    }
    
    public void setNewTotalPrice(BigDecimal newTotalPrice) {
        this.newTotalPrice = newTotalPrice;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    @Override
    public String toString() {
        return "BookingExtensionResponse{" +
                "bookingId=" + bookingId +
                ", originalCheckOutDate=" + originalCheckOutDate +
                ", newCheckOutDate=" + newCheckOutDate +
                ", additionalDays=" + additionalDays +
                ", originalPrice=" + originalPrice +
                ", additionalCost=" + additionalCost +
                ", newTotalPrice=" + newTotalPrice +
                ", message='" + message + '\'' +
                ", success=" + success +
                '}';
    }
}
