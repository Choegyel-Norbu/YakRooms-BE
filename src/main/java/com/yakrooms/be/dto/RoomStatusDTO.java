package com.yakrooms.be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/**
 * DTO for room status information
 * Used to avoid JPA proxy serialization issues
 */
public class RoomStatusDTO {
    
    @JsonProperty("roomNumber")
    private String roomNumber;
    
    @JsonProperty("roomType")
    private String roomType;
    
    @JsonProperty("roomStatus")
    private String roomStatus;
    
    @JsonProperty("guestName")
    private String guestName;
    
    @JsonProperty("checkOutDate")
    private LocalDate checkOutDate;

    // Default constructor
    public RoomStatusDTO() {}

    // Constructor with all fields
    public RoomStatusDTO(String roomNumber, String roomType, String roomStatus, 
                        String guestName, LocalDate checkOutDate) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.roomStatus = roomStatus;
        this.guestName = guestName;
        this.checkOutDate = checkOutDate;
    }

    // Getters and setters
    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    @Override
    public String toString() {
        return "RoomStatusDTO{" +
                "roomNumber='" + roomNumber + '\'' +
                ", roomType='" + roomType + '\'' +
                ", roomStatus='" + roomStatus + '\'' +
                ", guestName='" + guestName + '\'' +
                ", checkOutDate=" + checkOutDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomStatusDTO that = (RoomStatusDTO) o;

        if (roomNumber != null ? !roomNumber.equals(that.roomNumber) : that.roomNumber != null) return false;
        if (roomType != null ? !roomType.equals(that.roomType) : that.roomType != null) return false;
        if (roomStatus != null ? !roomStatus.equals(that.roomStatus) : that.roomStatus != null) return false;
        if (guestName != null ? !guestName.equals(that.guestName) : that.guestName != null) return false;
        return checkOutDate != null ? checkOutDate.equals(that.checkOutDate) : that.checkOutDate == null;
    }

    @Override
    public int hashCode() {
        int result = roomNumber != null ? roomNumber.hashCode() : 0;
        result = 31 * result + (roomType != null ? roomType.hashCode() : 0);
        result = 31 * result + (roomStatus != null ? roomStatus.hashCode() : 0);
        result = 31 * result + (guestName != null ? guestName.hashCode() : 0);
        result = 31 * result + (checkOutDate != null ? checkOutDate.hashCode() : 0);
        return result;
    }
}
