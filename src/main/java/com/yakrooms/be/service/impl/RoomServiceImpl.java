package com.yakrooms.be.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.yakrooms.be.dto.RoomResponseDTO;
import com.yakrooms.be.dto.mapper.RoomMapper;
import com.yakrooms.be.dto.request.RoomRequest;
import com.yakrooms.be.dto.response.RoomResponse;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.model.enums.RoomType;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.repository.RoomItemRepository;
import com.yakrooms.be.repository.RoomRepository;
import com.yakrooms.be.service.RoomService;

@Component
public class RoomServiceImpl implements RoomService {

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private HotelRepository hotelRepository;

	@Autowired
	private RoomItemRepository roomItemRepository;

	private final RoomMapper roomMapper;

	@Autowired
	RoomServiceImpl(RoomMapper roomMapper) {
		this.roomMapper = roomMapper;
	}

	@Override
	public boolean createRoom(Long hotelId, RoomRequest request) {
		Hotel hotel = hotelRepository.findById(hotelId)
				.orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

		Room room = roomMapper.toEntity(request);
		room.setAvailable(request.available);
		setRoomType(room, request.getRoomType());
		room.setHotel(hotel);

		Room savedRoom = roomRepository.save(room);
		return true;
	}

	@Override
	public RoomResponseDTO getRoomById(Long roomId) {
		Room room = roomRepository.findById(roomId)
				.orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
		return RoomMapper.toDto(room);
	}

	@Override
	public List<RoomResponseDTO> getRoomsByHotel(Long hotelId) {
		List<Room> rooms = roomRepository.findByHotelId(hotelId);
		return rooms.stream().map(RoomMapper::toDto).toList();
	}

	@Override
	public RoomResponseDTO updateRoom(Long roomId, RoomRequest request) {
		Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found"));

		roomMapper.updateRoomFromRequest(request, room);
		return RoomMapper.toDto(roomRepository.save(room));
	}

	@Override
	public void deleteRoom(Long roomId) {
		Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found"));
		roomRepository.delete(room);
	}

	@Override
	public RoomResponseDTO toggleAvailability(Long roomId, boolean isAvailable) {
		Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found"));
		room.setAvailable(isAvailable);
		return RoomMapper.toDto(roomRepository.save(room));
	}

	private void setRoomType(Room room, String type) {
		switch (type.toUpperCase()) {
		case "SINGLE":
			room.setRoomType(RoomType.SINGLE);
			break;
		case "DOUBLE":
			room.setRoomType(RoomType.DOUBLE);
			break;
		case "DELUXE":
			room.setRoomType(RoomType.DELUXE);
			break;
		case "SUITE":
			room.setRoomType(RoomType.SUITE);
			break;
		case "FAMILY":
			room.setRoomType(RoomType.FAMILY);
			break;
		case "TWIN":
			room.setRoomType(RoomType.TWIN);
			break;
		case "KING":
			room.setRoomType(RoomType.KING);
			break;
		case "QUEEN":
			room.setRoomType(RoomType.QUEEN);
			break;
		default:
			throw new IllegalArgumentException("Invalid room type: " + type);
		}
	}

	public Page<RoomResponseDTO> getAvailableRooms(Long hotelId, Pageable pageable) {
		return roomRepository.findActiveAvailableRoomsByHotelId(hotelId, pageable).map(RoomMapper::toDto);
	}

}
