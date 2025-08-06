package com.yakrooms.be.service;

import com.yakrooms.be.dto.BookingChangeEvent;

public interface BookingWebSocketService {
    void broadcastBookingStatusChange(BookingChangeEvent event);
    void broadcastToHotel(Long hotelId, BookingChangeEvent event);
    void broadcastToUser(Long userId, BookingChangeEvent event);
} 