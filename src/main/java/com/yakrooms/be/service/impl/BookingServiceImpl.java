package com.yakrooms.be.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.yakrooms.be.dto.mapper.BookingMapper;
import com.yakrooms.be.dto.request.BookingRequest;
import com.yakrooms.be.dto.response.BookingResponse;
import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.model.enums.BookingStatus;
import com.yakrooms.be.repository.BookingRepository;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.repository.RoomRepository;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.service.BookingService;

@Service
public class BookingServiceImpl implements BookingService {

	@Autowired
	private BookingRepository bookingRepository;
	
	@Autowired
	private RoomRepository roomRepository;
	
	@Autowired
	private HotelRepository hotelRepository;
	
	@Autowired
	private UserRepository userRepository;
	private final BookingMapper bookingMapper;
	
	@Autowired
	public BookingServiceImpl(BookingMapper bookingMapper) {
		this.bookingMapper = bookingMapper;
	}

	@Override
	public BookingResponse createBooking(BookingRequest request) {
		Room room = roomRepository.findById(request.roomId).orElseThrow(() -> new RuntimeException("Room not found"));

		if (!isRoomAvailable(room.getId(), request.checkInDate, request.checkOutDate)) {
			throw new RuntimeException("Room is not available for the selected dates");
		}

		Booking booking = bookingMapper.toEntity(request);
		booking.setRoom(room);
		booking.setHotel(room.getHotel());
		booking.setStatus(BookingStatus.PENDING);
		booking.setTotalPrice(room.getPrice() * (request.checkOutDate.toEpochDay() - request.checkInDate.toEpochDay()));
		// booking.setUser(authenticatedUser); // Set from context

		return bookingMapper.toDto(bookingRepository.save(booking));
	}

	@Override
	public void cancelBooking(Long bookingId, Long userId) {
		Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only cancel your own bookings");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
	}

	@Override
	public List<BookingResponse> getUserBookings(Long userId) {
		 return bookingRepository.findAllByUserId(userId).stream()
	                .map(bookingMapper::toDto)
	                .collect(Collectors.toList());
	}

	@Override
	public List<BookingResponse> getBookingsByHotel(Long hotelId) {
		 return bookingRepository.findAllByHotelId(hotelId).stream()
	                .map(bookingMapper::toDto)
	                .collect(Collectors.toList());
	}

	@Override
	public BookingResponse confirmBooking(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingMapper.toDto(bookingRepository.save(booking));
	}

	@Override
	public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
		List<Booking> bookings = bookingRepository.findBookingsForRoom(roomId, checkIn, checkOut);
        return bookings.isEmpty();
	}

	@Override
	public BookingResponse getBookingDetails(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        return bookingMapper.toDto(booking);
	}

}
