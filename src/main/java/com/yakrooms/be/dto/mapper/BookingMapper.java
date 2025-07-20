package com.yakrooms.be.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.yakrooms.be.dto.request.BookingRequest;
import com.yakrooms.be.dto.response.BookingResponse;
import com.yakrooms.be.model.entity.Booking;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookingMapper {
	
    Booking toEntity(BookingRequest request);
    
    public static BookingResponse toDTO(Booking booking) {
    	BookingResponse dto = new BookingResponse();
    	dto.setUserId(booking.getUser().getId());
    	dto.setRoomId(booking.getRoom().getId());
    	dto.setCheckInDate(booking.getCheckInDate());
    	dto.setCheckOutDate(booking.getCheckOutDate());
    	dto.setGuests(booking.getGuests());
    	dto.setId(booking.getId());
    	dto.setStatus(booking.getStatus());
    	dto.setTotalPrice(booking.getTotalPrice());
    	dto.setName(booking.getUser().getName());
    	dto.setPhone(booking.getUser().getPhone());
    	dto.setEmail(booking.getUser().getEmail());
    	dto.setRoomNumber(booking.getRoom().getRoomNumber());
    	return dto;
    }
    
    BookingResponse toDto(Booking booking);

}