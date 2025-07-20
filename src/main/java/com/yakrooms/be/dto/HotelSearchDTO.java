package com.yakrooms.be.dto;

public class HotelSearchDTO {
    private String location; // could be district, city, or village
    private String hotelType; // e.g., "Resort", "Homestay", "Lodge"

    // Constructors
    public HotelSearchDTO() {}

    public HotelSearchDTO(String location, String hotelType) {
        this.location = location;
        this.hotelType = hotelType;
    }

    // Getters and Setters
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getHotelType() {
        return hotelType;
    }

    public void setHotelType(String hotelType) {
        this.hotelType = hotelType;
    }
}