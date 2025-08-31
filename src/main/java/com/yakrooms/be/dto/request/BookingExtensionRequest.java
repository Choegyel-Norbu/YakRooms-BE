package com.yakrooms.be.dto.request;

import java.time.LocalDate;
import jakarta.validation.constraints.*;

/**
 * DTO for extending an existing booking's stay duration.
 * This allows guests to extend their stay beyond the original check-out date.
 * 
 * @author YakRooms Team
 * @version 1.0
 */
public class BookingExtensionRequest {
    
    @NotNull(message = "New check-out date is required")
    @Future(message = "New check-out date must be in the future")
    private LocalDate newCheckOutDate;
    
    @Min(value = 1, message = "At least 1 guest is required")
    @Max(value = 20, message = "Maximum 20 guests allowed")
    private Integer guests;
    
    @Pattern(regexp = "^[+]?[0-9\\-\\s()]+$", message = "Invalid phone number format")
    private String phone;
    
    @Size(max = 255, message = "Destination cannot exceed 255 characters")
    private String destination;
    
    @Size(max = 255, message = "Origin cannot exceed 255 characters")
    private String origin;
    
    // Constructors
    public BookingExtensionRequest() {}
    
    public BookingExtensionRequest(LocalDate newCheckOutDate) {
        this.newCheckOutDate = newCheckOutDate;
    }
    
    public BookingExtensionRequest(LocalDate newCheckOutDate, Integer guests, String phone, 
                                  String destination, String origin) {
        this.newCheckOutDate = newCheckOutDate;
        this.guests = guests;
        this.phone = phone;
        this.destination = destination;
        this.origin = origin;
    }
    
    // Getters and Setters
    public LocalDate getNewCheckOutDate() {
        return newCheckOutDate;
    }
    
    public void setNewCheckOutDate(LocalDate newCheckOutDate) {
        this.newCheckOutDate = newCheckOutDate;
    }
    
    public Integer getGuests() {
        return guests;
    }
    
    public void setGuests(Integer guests) {
        this.guests = guests;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public String getOrigin() {
        return origin;
    }
    
    public void setOrigin(String origin) {
        this.origin = origin;
    }
    
    @Override
    public String toString() {
        return "BookingExtensionRequest{" +
                "newCheckOutDate=" + newCheckOutDate +
                ", guests=" + guests +
                ", phone='" + phone + '\'' +
                ", destination='" + destination + '\'' +
                ", origin='" + origin + '\'' +
                '}';
    }
}
