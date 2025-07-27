package com.yakrooms.be.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yakrooms.be.dto.RoomResponseDTO;
import com.yakrooms.be.dto.RoomStatusDTO;
import com.yakrooms.be.dto.mapper.RoomMapper;
import com.yakrooms.be.dto.request.RoomRequest;
import com.yakrooms.be.dto.response.RoomResponse;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.model.enums.RoomType;
import com.yakrooms.be.projection.RoomStatusProjection;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.repository.RoomItemRepository;
import com.yakrooms.be.repository.RoomRepository;
import com.yakrooms.be.service.RoomService;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomServiceImpl.class);

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomItemRepository roomItemRepository;
    private final RoomMapper roomMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository,
                          HotelRepository hotelRepository,
                          RoomItemRepository roomItemRepository,
                          RoomMapper roomMapper,
                          SimpMessagingTemplate messagingTemplate) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.roomItemRepository = roomItemRepository;
        this.roomMapper = roomMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public RoomResponseDTO createRoom(Long hotelId, RoomRequest request) {
        // Validate input
        if (hotelId == null || request == null) {
            throw new IllegalArgumentException("Hotel ID and room request cannot be null");
        }

        // Find hotel
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));

        // Create room using mapper (it handles the enum conversion)
        Room room = roomMapper.toEntity(request);
        room.setHotel(hotel);

        Room savedRoom = roomRepository.save(room);
        logger.info("Created new room with ID: {} for hotel: {}", savedRoom.getId(), hotelId);

        return roomMapper.toDto(savedRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponseDTO getRoomById(Long roomId) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
        
        return roomMapper.toDto(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getRoomsByHotel(Long hotelId) {
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }

        List<Room> rooms = roomRepository.findByHotelId(hotelId);
        return rooms.stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public RoomResponseDTO updateRoom(Long roomId, RoomRequest request) {
        // Validate input
        if (roomId == null || request == null) {
            throw new IllegalArgumentException("Room ID and request cannot be null");
        }

        // Find and update room
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        // Update room using mapper
        roomMapper.updateRoomFromRequest(request, room);
        Room updatedRoom = roomRepository.save(room);

        // Get hotelId for WebSocket notification
        Long hotelId = updatedRoom.getHotel() != null ? updatedRoom.getHotel().getId() : null;

        // Broadcast updated room list via WebSocket
        if (hotelId != null) {
            try {
                List<Room> activeRooms = roomRepository
                        .findActiveAvailableRoomsByHotelId(hotelId, Pageable.unpaged())
                        .getContent();

                List<RoomResponseDTO> activeRoomDTOs = activeRooms.stream()
                        .map(roomMapper::toDto)
                        .collect(Collectors.toList());

                messagingTemplate.convertAndSend("/topic/rooms/" + hotelId, activeRoomDTOs);
                logger.debug("Broadcasted room updates for hotel: {}", hotelId);
            } catch (Exception e) {
                logger.error("Failed to broadcast room updates for hotel: {}", hotelId, e);
                // Don't throw exception here as room update was successful
            }
        }

        logger.info("Updated room with ID: {}", roomId);
        return roomMapper.toDto(updatedRoom);
    }

    @Override
    public void deleteRoom(Long roomId) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }

        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room not found with id: " + roomId);
        }

        roomRepository.deleteById(roomId);
        logger.info("Deleted room with ID: {}", roomId);
    }

    @Override
    public RoomResponseDTO toggleAvailability(Long roomId, boolean isAvailable) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        room.setAvailable(isAvailable);
        Room savedRoom = roomRepository.save(room);
        
        logger.info("Toggled availability for room ID: {} to: {}", roomId, isAvailable);
        return roomMapper.toDto(savedRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomResponseDTO> getAvailableRooms(Long hotelId, Pageable pageable) {
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }

        return roomRepository.findActiveAvailableRoomsByHotelId(hotelId, pageable)
                .map(roomMapper::toDto);
    }

    // Additional method to get RoomResponse instead of RoomResponseDTO
    @Transactional(readOnly = true)
    public RoomResponse getRoomResponseById(Long roomId) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
        
        return roomMapper.toDtoResponse(room);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomResponsesByHotel(Long hotelId) {
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }

        List<Room> rooms = roomRepository.findByHotelId(hotelId);
        return rooms.stream()
                .map(roomMapper::toDtoResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<RoomResponse> getAvailableRoomResponses(Long hotelId, Pageable pageable) {
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }

        return roomRepository.findActiveAvailableRoomsByHotelId(hotelId, pageable)
                .map(roomMapper::toDtoResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomStatusProjection> getRoomStatusByHotelId(Long hotelId, Pageable pageable) {
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
        return roomRepository.getRoomStatusByHotelId(hotelId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomStatusProjection> getRoomStatusByHotelIdAndRoomNumber(Long hotelId, String roomNumber, Pageable pageable) {
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Room number cannot be null or empty");
        }
        return roomRepository.getRoomStatusByHotelIdAndRoomNumber(hotelId, roomNumber.trim(), pageable);
    }
}