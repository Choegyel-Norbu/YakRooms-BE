package com.yakrooms.be.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO representing booked dates for a room.
 * Simple response for frontend date blocking.
 * 
 * @author YakRooms Team
 * @version 1.0
 */
public class RoomBookedDatesDTO {
    
    private Long roomId;
    private String roomNumber;
    private List<LocalDate> bookedDates;
    
    // Default constructor
    public RoomBookedDatesDTO() {}
    
    // Constructor with required fields
    public RoomBookedDatesDTO(Long roomId, String roomNumber, List<LocalDate> bookedDates) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.bookedDates = bookedDates;
    }
    
    // Getters and Setters
    public Long getRoomId() {
        return roomId;
    }
    
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
    
    public String getRoomNumber() {
        return roomNumber;
    }
    
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
    
    public List<LocalDate> getBookedDates() {
        return bookedDates;
    }
    
    public void setBookedDates(List<LocalDate> bookedDates) {
        this.bookedDates = bookedDates;
    }
    
    @Override
    public String toString() {
        return "RoomBookedDatesDTO{" +
                "roomId=" + roomId +
                ", roomNumber='" + roomNumber + '\'' +
                ", bookedDates=" + bookedDates +
                '}';
    }
}
