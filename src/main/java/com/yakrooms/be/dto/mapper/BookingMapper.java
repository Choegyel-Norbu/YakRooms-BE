package com.yakrooms.be.dto.mapper;

import org.springframework.stereotype.Component;

import com.yakrooms.be.dto.request.BookingRequest;
import com.yakrooms.be.dto.response.BookingResponse;
import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.BookingStatus;

@Component
public class BookingMapper {
	
	public Booking toEntity(BookingRequest request) {
        if (request == null) {
            return null;
        }

        Booking booking = new Booking();
        
        // Set User (you'll need to fetch from repository)
        if (request.getUserId() != null) {
            User user = new User();
            user.setId(request.getUserId());
            booking.setUser(user);
        }
        
        // Set Hotel (you'll need to fetch from repository)
        if (request.getHotelId() != null) {
            Hotel hotel = new Hotel();
            hotel.setId(request.getHotelId());
            booking.setHotel(hotel);
        }
        
        // Set Room (you'll need to fetch from repository)
        if (request.getRoomId() != null) {
            Room room = new Room();
            room.setId(request.getRoomId());
            booking.setRoom(room);
        }
        
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setGuests(request.getGuests());
        booking.setTotalPrice(request.getTotalPrice());
        
        return booking;
    }

    public BookingResponse toDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingResponse response = new BookingResponse();
        
        response.setId(booking.getId());
        
        // Extract userId from User entity
        if (booking.getUser() != null) {
            response.setUserId(booking.getUser().getId());
        }
        
        // Extract roomId from Room entity
        if (booking.getRoom() != null) {
            response.setRoomId(booking.getRoom().getId());
        }
        
        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());
        response.setGuests(booking.getGuests());
        response.setStatus(booking.getStatus());
        response.setPhone(booking.getPhone());
        response.setTotalPrice(booking.getTotalPrice());
        response.setCreatedAt(booking.getCreatedAt());
        response.setPasscode(booking.getPasscode());
        
        // These fields seem to be additional user/room details
        // You may need to populate these from the related entities
        if (booking.getUser() != null) {
            response.setName(booking.getUser().getName()); // Assuming User has getName()
            response.setEmail(booking.getUser().getEmail()); // Assuming User has getEmail()
        }
        
        if (booking.getRoom() != null) {
            response.setRoomNumber(booking.getRoom().getRoomNumber()); // Assuming Room has getRoomNumber()
        }
        
        // Add hotel information
        if (booking.getHotel() != null) {
            response.setHotelName(booking.getHotel().getName());
            response.setHotelDistrict(booking.getHotel().getDistrict());
        }
        
        return response;
    }

    // Alternative toEntity method that accepts full entities (when you have them from repositories)
    public Booking toEntity(BookingRequest request, User user, Hotel hotel, Room room) {
        if (request == null) {
            return null;
        }

        Booking booking = new Booking();
        
        booking.setUser(user);
        booking.setHotel(hotel);
        booking.setRoom(room);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setGuests(request.getGuests());
        booking.setTotalPrice(request.getTotalPrice());
        
        return booking;
    }

    // Method to create entity for booking creation (sets default status)
    public Booking toEntityForCreation(BookingRequest request, User user, Hotel hotel, Room room) {
        if (request == null) {
            return null;
        }

        Booking booking = new Booking();
        
        booking.setUser(user);
        booking.setHotel(hotel);
        booking.setRoom(room);
        booking.setPhone(request.getPhone());
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setGuests(request.getGuests());
        booking.setTotalPrice(request.getTotalPrice());
        booking.setStatus(BookingStatus.CONFIRMED); // Default status for new bookings
        
        return booking;
    }

    // Method to update existing entity from request
    public void updateEntityFromRequest(Booking booking, BookingRequest request) {
        if (booking == null || request == null) {
            return;
        }
        
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setGuests(request.getGuests());
        booking.setTotalPrice(request.getTotalPrice());
        // Note: User, Hotel, Room relationships should be handled separately
        // as they require repository lookups
    }
}
