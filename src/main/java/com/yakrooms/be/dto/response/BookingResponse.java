package com.yakrooms.be.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingResponse {
    public Long id;
    public Long userId;
    public Long hotelId;
    public Long roomId;
    public LocalDate checkInDate;
    public LocalDate checkOutDate;
    public int guests;
    public String status;
    public Double totalPrice;
    public LocalDateTime createdAt;
}