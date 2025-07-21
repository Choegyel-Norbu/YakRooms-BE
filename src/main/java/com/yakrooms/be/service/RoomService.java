package com.yakrooms.be.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yakrooms.be.dto.RoomResponseDTO;
import com.yakrooms.be.dto.request.RoomRequest;
import com.yakrooms.be.dto.response.RoomResponse;

@Service
public interface RoomService {
	RoomResponseDTO createRoom(Long hotelId, RoomRequest request);

	RoomResponseDTO getRoomById(Long roomId);

	List<RoomResponseDTO> getRoomsByHotel(Long hotelId);

	RoomResponseDTO updateRoom(Long roomId, RoomRequest request);

	void deleteRoom(Long roomId);

	RoomResponseDTO toggleAvailability(Long roomId, boolean isAvailable);

	public Page<RoomResponseDTO> getAvailableRooms(Long hotelId, Pageable pageable);

}