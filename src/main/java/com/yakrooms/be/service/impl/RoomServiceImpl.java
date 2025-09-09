package com.yakrooms.be.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;

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
import com.yakrooms.be.dto.mapper.RoomStatusMapper;
import com.yakrooms.be.dto.request.RoomRequest;
import com.yakrooms.be.dto.response.RoomResponse;
import com.yakrooms.be.dto.RoomBookedDatesDTO;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.projection.RoomStatusProjection;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.repository.RoomRepository;
import com.yakrooms.be.repository.BookingRepository;
import com.yakrooms.be.service.RoomService;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomServiceImpl.class);

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomMapper roomMapper;
    private final RoomStatusMapper roomStatusMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final BookingRepository bookingRepository;

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository,
                          HotelRepository hotelRepository,
                          RoomMapper roomMapper,
                          RoomStatusMapper roomStatusMapper,
                          SimpMessagingTemplate messagingTemplate,
                          BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.roomMapper = roomMapper;
        this.roomStatusMapper = roomStatusMapper;
        this.messagingTemplate = messagingTemplate;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public RoomResponseDTO createRoom(Long hotelId, RoomRequest request) {
        validateInput(hotelId, request, "Hotel ID and room request cannot be null");

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));

        Room room = roomMapper.toEntity(request);
        room.setHotel(hotel);

        Room savedRoom = roomRepository.save(room);
        logger.info("Created new room with ID: {} for hotel: {}", savedRoom.getId(), hotelId);

        return roomMapper.toDto(savedRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponseDTO getRoomById(Long roomId) {
        validateInput(roomId, "Room ID cannot be null");

        Room room = roomRepository.findByIdWithItems(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
        
        // Fetch collections separately to avoid MultipleBagFetchException
        setRoomCollections(room);
        
        return roomMapper.toDto(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getRoomsByHotel(Long hotelId) {
        validateInput(hotelId, "Hotel ID cannot be null");
        
        // Use optimized batch fetching
        List<Room> rooms = getRoomsWithCollectionsBatch(hotelId);
        
        return rooms.stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public RoomResponseDTO updateRoom(Long roomId, RoomRequest request) {
        validateInput(roomId, request, "Room ID and request cannot be null");

        Room room = roomRepository.findByIdWithItems(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        roomMapper.updateRoomFromRequest(request, room);
        Room updatedRoom = roomRepository.save(room);

        logger.info("Updated room with ID: {}", roomId);
        
        // Broadcast updates via WebSocket in a separate transaction
        try {
            broadcastRoomUpdates(updatedRoom);
        } catch (Exception e) {
            logger.error("Failed to broadcast room updates, but room update was successful", e);
        }
        
        return roomMapper.toDto(updatedRoom);
    }

    @Override
    public void deleteRoom(Long roomId) {
        validateInput(roomId, "Room ID cannot be null");

        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room not found with id: " + roomId);
        }

        roomRepository.deleteById(roomId);
        logger.info("Deleted room with ID: {}", roomId);
    }

    @Override
    public RoomResponseDTO toggleAvailability(Long roomId, boolean isAvailable) {
        validateInput(roomId, "Room ID cannot be null");

        Room room = roomRepository.findByIdWithItems(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        room.setAvailable(isAvailable);
        Room savedRoom = roomRepository.save(room);
        
        logger.info("Toggled availability for room ID: {} to: {}", roomId, isAvailable);
        return roomMapper.toDto(savedRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomResponseDTO> getAvailableRooms(Long hotelId, Pageable pageable) {
        validateInput(hotelId, "Hotel ID cannot be null");

        // Option A: Use basic data query + batch fetch collections (most efficient)
        Page<Room> roomPage = roomRepository.findActiveAvailableRoomsBasicData(hotelId, pageable);
        
        if (roomPage.isEmpty()) {
            return roomPage.map(roomMapper::toDto);
        }
        
        // Batch fetch collections for all rooms in the page
        List<Long> roomIds = roomPage.getContent().stream()
                .map(Room::getId)
                .collect(Collectors.toList());
        
        Map<Long, List<String>> amenitiesMap = buildAmenitiesMapForRooms(roomIds);
        Map<Long, List<String>> imageUrlsMap = buildImageUrlsMapForRooms(roomIds);
        
        // Set collections for each room
        roomPage.getContent().forEach(room -> {
            room.setAmenities(amenitiesMap.getOrDefault(room.getId(), new ArrayList<>()));
            room.setImageUrl(imageUrlsMap.getOrDefault(room.getId(), new ArrayList<>()));
        });

        return roomPage.map(roomMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomStatusDTO> getRoomStatusByHotelIdAndRoomNumber(Long hotelId, String roomNumber, Pageable pageable) {
        validateInput(hotelId, "Hotel ID cannot be null");
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Room number cannot be null or empty");
        }
        Page<RoomStatusProjection> projectionPage = roomRepository.getRoomStatusByHotelIdAndRoomNumber(hotelId, roomNumber.trim(), pageable);
        return roomStatusMapper.toDtoPage(projectionPage);
    }

    // Alternative methods returning RoomResponse instead of RoomResponseDTO
    @Transactional(readOnly = true)
    public RoomResponse getRoomResponseById(Long roomId) {
        validateInput(roomId, "Room ID cannot be null");

        Room room = roomRepository.findByIdWithItems(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
        
        setRoomCollections(room);
        return roomMapper.toDtoResponse(room);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomResponsesByHotel(Long hotelId) {
        validateInput(hotelId, "Hotel ID cannot be null");
        
        List<Room> rooms = getRoomsWithCollectionsBatch(hotelId);
        
        return rooms.stream()
                .map(roomMapper::toDtoResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<RoomResponse> getAvailableRoomResponses(Long hotelId, Pageable pageable) {
        validateInput(hotelId, "Hotel ID cannot be null");

        return roomRepository.findActiveAvailableRoomsByHotelId(hotelId, pageable)
                .map(roomMapper::toDtoResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomBookedDatesDTO getBookedDatesForRoom(Long roomId) {
        validateInput(roomId, "Room ID cannot be null");
        
        // Validate room exists
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
        
        // Get all active bookings for the room
        List<Booking> activeBookings = bookingRepository.findAllActiveBookingsByRoomId(roomId);
        
        // Extract all booked dates from the bookings
        List<LocalDate> bookedDates = new ArrayList<>();
        for (Booking booking : activeBookings) {
            LocalDate currentDate = booking.getCheckInDate();
            LocalDate endDate = booking.getCheckOutDate();
            
            // Add all dates from check-in to check-out (exclusive)
            while (currentDate.isBefore(endDate)) {
                bookedDates.add(currentDate);
                currentDate = currentDate.plusDays(1);
            }
        }
        
        // Remove duplicates and sort
        List<LocalDate> uniqueBookedDates = bookedDates.stream()
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        
        return new RoomBookedDatesDTO(roomId, room.getRoomNumber(), uniqueBookedDates);
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Optimized batch fetching of rooms with all collections
     * Query count: 3 total (1 for rooms, 1 for amenities, 1 for imageUrls)
     */
    private List<Room> getRoomsWithCollectionsBatch(Long hotelId) {
        // Fetch rooms (1 query)
        List<Room> rooms = roomRepository.findByHotelIdWithItems(hotelId);
        
        if (rooms.isEmpty()) {
            return rooms;
        }
        
        // Batch fetch collections for all rooms (2 queries)
        Map<Long, List<String>> amenitiesMap = buildAmenitiesMap(hotelId);
        Map<Long, List<String>> imageUrlsMap = buildImageUrlsMap(hotelId);
        
        // Set collections for each room
        rooms.forEach(room -> {
            room.setAmenities(amenitiesMap.getOrDefault(room.getId(), new ArrayList<>()));
            room.setImageUrl(imageUrlsMap.getOrDefault(room.getId(), new ArrayList<>()));
        });
        
        return rooms;
    }

    /**
     * Set collections for a single room
     * Query count: 2 (1 for amenities, 1 for imageUrls)
     */
    private void setRoomCollections(Room room) {
        List<String> amenities = roomRepository.findAmenitiesByRoomId(room.getId());
        List<String> imageUrls = roomRepository.findImageUrlsByRoomId(room.getId());
        room.setAmenities(amenities);
        room.setImageUrl(imageUrls);
    }

    /**
     * Build amenities map for batch processing
     */
    private Map<Long, List<String>> buildAmenitiesMap(Long hotelId) {
        List<Object[]> amenitiesData = roomRepository.findAmenitiesByHotelIdWithRoomId(hotelId);
        Map<Long, List<String>> amenitiesMap = new HashMap<>();
        
        amenitiesData.forEach(data -> {
            Long roomId = (Long) data[0];
            String amenity = (String) data[1];
            amenitiesMap.computeIfAbsent(roomId, k -> new ArrayList<>()).add(amenity);
        });
        
        return amenitiesMap;
    }

    /**
     * Build image URLs map for batch processing
     */
    private Map<Long, List<String>> buildImageUrlsMap(Long hotelId) {
        List<Object[]> imageUrlsData = roomRepository.findImageUrlsByHotelIdWithRoomId(hotelId);
        Map<Long, List<String>> imageUrlsMap = new HashMap<>();
        
        imageUrlsData.forEach(data -> {
            Long roomId = (Long) data[0];
            String imageUrl = (String) data[1];
            imageUrlsMap.computeIfAbsent(roomId, k -> new ArrayList<>()).add(imageUrl);
        });
        
        return imageUrlsMap;
    }

    /**
     * Broadcast room updates via WebSocket
     */
    private void broadcastRoomUpdates(Room updatedRoom) {
        Long hotelId = updatedRoom.getHotel() != null ? updatedRoom.getHotel().getId() : null;

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
    }

    /**
     * Validation helper methods
     */
    private void validateInput(Object input, String errorMessage) {
        if (input == null) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void validateInput(Object input1, Object input2, String errorMessage) {
        if (input1 == null || input2 == null) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
    
    private Map<Long, List<String>> buildAmenitiesMapForRooms(List<Long> roomIds) {
        List<Object[]> amenitiesData = roomRepository.findAmenitiesByRoomIds(roomIds);
        Map<Long, List<String>> amenitiesMap = new HashMap<>();
        
        amenitiesData.forEach(data -> {
            Long roomId = (Long) data[0];
            String amenity = (String) data[1];
            amenitiesMap.computeIfAbsent(roomId, k -> new ArrayList<>()).add(amenity);
        });
        
        return amenitiesMap;
    }

    private Map<Long, List<String>> buildImageUrlsMapForRooms(List<Long> roomIds) {
        List<Object[]> imageUrlsData = roomRepository.findImageUrlsByRoomIds(roomIds);
        Map<Long, List<String>> imageUrlsMap = new HashMap<>();
        
        imageUrlsData.forEach(data -> {
            Long roomId = (Long) data[0];
            String imageUrl = (String) data[1];
            imageUrlsMap.computeIfAbsent(roomId, k -> new ArrayList<>()).add(imageUrl);
        });
        
        return imageUrlsMap;
    }

	@Override
	@Transactional(readOnly = true)
	public Page<RoomStatusDTO> getRoomStatusByHotelId(Long hotelId, Pageable pageable) {
		validateInput(hotelId, "Hotel ID cannot be null");
		Page<RoomStatusProjection> projectionPage = roomRepository.getRoomStatusByHotelId(hotelId, pageable);
		return roomStatusMapper.toDtoPage(projectionPage);
	}
    
}