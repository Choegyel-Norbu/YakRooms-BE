package com.yakrooms.be.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yakrooms.be.dto.RoomResponseDTO;
import com.yakrooms.be.dto.RoomStatusDTO;
import com.yakrooms.be.dto.RoomBookedDatesDTO;
import com.yakrooms.be.dto.request.RoomRequest;
import com.yakrooms.be.dto.response.RoomResponse;
import com.yakrooms.be.projection.RoomStatusProjection;

@Service
public interface RoomService {
	RoomResponseDTO createRoom(Long hotelId, RoomRequest request);

	RoomResponseDTO getRoomById(Long roomId);

	List<RoomResponseDTO> getRoomsByHotel(Long hotelId);

	RoomResponseDTO updateRoom(Long roomId, RoomRequest request);

	void deleteRoom(Long roomId);

	RoomResponseDTO toggleAvailability(Long roomId, boolean isAvailable);

	public Page<RoomResponseDTO> getAvailableRooms(Long hotelId, Pageable pageable);

	Page<RoomStatusProjection> getRoomStatusByHotelId(Long hotelId, Pageable pageable);
	
	Page<RoomStatusProjection> getRoomStatusByHotelIdAndRoomNumber(Long hotelId, String roomNumber, Pageable pageable);
	
	/**
	 * Get all booked dates for a specific room.
	 * This method returns dates that should be blocked in frontend date pickers.
	 * 
	 * @param roomId The room ID
	 * @return RoomBookedDatesDTO with room info and list of booked dates
	 */
	RoomBookedDatesDTO getBookedDatesForRoom(Long roomId);
}