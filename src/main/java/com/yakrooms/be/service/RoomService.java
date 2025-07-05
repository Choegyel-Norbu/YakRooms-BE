package com.yakrooms.be.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yakrooms.be.dto.request.RoomRequest;
import com.yakrooms.be.dto.response.RoomResponse;

@Service
public interface RoomService {
    RoomResponse createRoom(Long hotelId, RoomRequest request);
    RoomResponse getRoomById(Long roomId);
    List<RoomResponse> getRoomsByHotel(Long hotelId);
    RoomResponse updateRoom(Long roomId, RoomRequest request);
    void deleteRoom(Long roomId);
    RoomResponse toggleAvailability(Long roomId, boolean isAvailable);
}