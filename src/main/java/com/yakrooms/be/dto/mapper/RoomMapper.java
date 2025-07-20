package com.yakrooms.be.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.yakrooms.be.dto.RoomResponseDTO;
import com.yakrooms.be.dto.request.RoomRequest;
import com.yakrooms.be.dto.response.RoomResponse;
import com.yakrooms.be.model.entity.Room;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoomMapper {
	
	public static RoomResponseDTO toDto(Room room) {
        RoomResponseDTO dto = new RoomResponseDTO();
        dto.setId(room.getId());
        dto.setRoomType(room.getRoomType());
        dto.setDescription(room.getDescription());
        dto.setPrice(room.getPrice());
        dto.setMaxGuests(room.getMaxGuests());
        dto.setHotelName(room.getHotel() != null ? room.getHotel().getName() : null);
        dto.setAmenities(room.getAmenities());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setImageUrl(room.getImageUrl());
        dto.setAvailable(room.getAvailable());
        return dto;
    }
    Room toEntity(RoomRequest request);
    void updateRoomFromRequest(RoomRequest request, @MappingTarget Room room);
}