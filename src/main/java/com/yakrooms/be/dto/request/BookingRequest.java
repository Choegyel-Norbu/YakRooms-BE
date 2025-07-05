package com.yakrooms.be.dto.request;

import java.time.LocalDate;

public class BookingRequest {
    public Long hotelId;
    public Long roomId;
    public LocalDate checkInDate;
    public LocalDate checkOutDate;
    public int guests;
}