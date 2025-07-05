package com.yakrooms.be.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yakrooms.be.dto.mapper.RoomMapper;
import com.yakrooms.be.dto.request.RoomRequest;
import com.yakrooms.be.dto.response.RoomResponse;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.repository.RoomRepository;
import com.yakrooms.be.service.RoomService;

@Component
public class RoomServiceImpl implements RoomService{
	
	@Autowired
	private  RoomRepository roomRepository;
	
    @Autowired
	private  HotelRepository hotelRepository;
    
    private final RoomMapper roomMapper;
    
    @Autowired
    RoomServiceImpl(RoomMapper roomMapper){
    	this.roomMapper = roomMapper;
    }

    @Override
    public RoomResponse createRoom(Long hotelId, RoomRequest request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        Room room = roomMapper.toEntity(request);
        room.setHotel(hotel);
        room.setAvailable(true);

        return roomMapper.toDto(roomRepository.save(room));
    }

    @Override
    public RoomResponse getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        return roomMapper.toDto(room);
    }

    @Override
    public List<RoomResponse> getRoomsByHotel(Long hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel not found");
        }

        return roomRepository.findAllByHotelId(hotelId).stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public RoomResponse updateRoom(Long roomId, RoomRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        roomMapper.updateRoomFromRequest(request, room);
        return roomMapper.toDto(roomRepository.save(room));
    }

    @Override
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        roomRepository.delete(room);
    }

    @Override
    public RoomResponse toggleAvailability(Long roomId, boolean isAvailable) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        room.setAvailable(isAvailable);
        return roomMapper.toDto(roomRepository.save(room));
    }
}
