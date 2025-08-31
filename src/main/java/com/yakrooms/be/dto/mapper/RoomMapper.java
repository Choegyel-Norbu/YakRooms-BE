package com.yakrooms.be.dto.mapper;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

import com.yakrooms.be.dto.RoomResponseDTO;
import com.yakrooms.be.dto.request.RoomRequest;
import com.yakrooms.be.dto.response.RoomResponse;
import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.model.enums.RoomType;

@Component
public class RoomMapper {

    public RoomResponseDTO toDto(Room room) {
        if (room == null) {
            return null;
        }

        RoomResponseDTO dto = new RoomResponseDTO();
        dto.setId(room.getId());
        dto.setRoomType(room.getRoomType());
        dto.setDescription(room.getDescription());
        dto.setPrice(room.getPrice());
        dto.setMaxGuests(room.getMaxGuests());
        dto.setHotelName(room.getHotel() != null ? room.getHotel().getName() : null);
        dto.setRoomNumber(room.getRoomNumber());
        dto.setImageUrl(room.getImageUrl());
        dto.setAvailable(room.getAvailable());
        
        // Copy amenities list (create new list to avoid reference issues)
        if (room.getAmenities() != null) {
            dto.setAmenities(new ArrayList<>(room.getAmenities()));
        }

        return dto;
    }

    public RoomResponse toDtoResponse(Room room) {
        if (room == null) {
            return null;
        }

        RoomResponse response = new RoomResponse();
        response.id = room.getId();
        response.roomType = room.getRoomType();
        response.description = room.getDescription();
        response.price = room.getPrice();
        response.maxGuests = room.getMaxGuests();
        response.roomNumber = Integer.parseInt(room.getRoomNumber());
        response.isAvailable = room.getAvailable();
        response.createdAt = room.getCreatedAt();
        response.updatedAt = room.getUpdatedAt();
        
        // Copy amenities list (create new list to avoid reference issues)
        if (room.getAmenities() != null) {
            response.amenities = new ArrayList<>(room.getAmenities());
        }

        return response;
    }

    public Room toEntity(RoomRequest request) {
        if (request == null) {
            return null;
        }

        Room room = new Room();
        
        // Convert String roomType to RoomType enum
        if (request.getRoomType() != null) {
            try {
                room.setRoomType(RoomType.valueOf(request.getRoomType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Handle invalid room type - could log warning or set default
                room.setRoomType(null);
            }
        }
        
        room.setDescription(request.getDescription());
        room.setPrice(request.getPrice());
        room.setMaxGuests(request.getMaxGuests());
        room.setRoomNumber(request.getRoomNumber());
        room.setAvailable(request.isAvailable());
        
        // Copy lists (create new lists to avoid reference issues)
        if (request.getAmenities() != null) {
            room.setAmenities(new ArrayList<>(request.getAmenities()));
        }
        
        if (request.getImageUrl() != null) {
            room.setImageUrl(new ArrayList<>(request.getImageUrl()));
        }

        return room;
    }

    public void updateRoomFromRequest(RoomRequest request, Room room) {
        if (request == null || room == null) {
            return;
        }

        // Update only non-null values (following IGNORE strategy)
        if (request.getRoomType() != null) {
            try {
                room.setRoomType(RoomType.valueOf(request.getRoomType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Handle invalid room type - could log warning or keep existing value
            }
        }
        
        if (request.getDescription() != null) {
            room.setDescription(request.getDescription());
        }
        
        if (request.getPrice() != null) {
            room.setPrice(request.getPrice());
        }
        
        if (request.getMaxGuests() > 0) { // primitive int, check for valid value
            room.setMaxGuests(request.getMaxGuests());
        }
        
        if (request.getRoomNumber() != null) {
            room.setRoomNumber(request.getRoomNumber());
        }
        
        // boolean primitive - always update
        // room.setAvailable(request.isAvailable());
        
        // Update lists only if they are not null
        if (request.getAmenities() != null) {
            room.setAmenities(new ArrayList<>(request.getAmenities()));
        }
        
        if (request.getImageUrl() != null) {
            room.setImageUrl(new ArrayList<>(request.getImageUrl()));
        }
    }
}