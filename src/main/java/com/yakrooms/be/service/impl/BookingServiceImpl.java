package com.yakrooms.be.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yakrooms.be.dto.mapper.BookingMapper;
import com.yakrooms.be.dto.request.BookingRequest;
import com.yakrooms.be.dto.response.BookingResponse;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.model.entity.User;
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
		User user = userRepository.findById(request.getUserId())
				.orElseThrow(() -> new RuntimeException("User not found"));

		Room room = roomRepository.findById(request.getRoomId())
				.orElseThrow(() -> new RuntimeException("Room not found"));

		Hotel hotel = hotelRepository.findById(request.getHotelId())
				.orElseThrow(() -> new RuntimeException("Hotel not found"));

		Booking booking = new Booking();
		booking.setUser(user);
		booking.setRoom(room);
		booking.setHotel(hotel);
		booking.setCheckInDate(request.getCheckInDate());
		booking.setCheckOutDate(request.getCheckOutDate());
		booking.setGuests(request.getGuests());
		booking.setStatus(BookingStatus.CONFIRMED);
		booking.setTotalPrice(request.getTotalPrice());

		Booking saved = bookingRepository.save(booking);
		return bookingMapper.toDto(saved);
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
		return bookingRepository.findAllByUserId(userId).stream().map(bookingMapper::toDto)
				.collect(Collectors.toList());
	}

	@Override
	public List<BookingResponse> getBookingsByHotel(Long hotelId) {
		return bookingRepository.findAllByHotelId(hotelId).stream().map(bookingMapper::toDto)
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

	@Override
	public List<BookingResponse> listAllBookingNoPaginaiton() {
		List<Booking> bookings = bookingRepository.findAll();

		List<BookingResponse> responses = new ArrayList<>();

		for (Booking booking : bookings) {
			if (booking.getUser() != null && booking.getRoom() != null) {
				BookingResponse dto = BookingMapper.toDTO(booking);
				responses.add(dto);
			} else {
				System.out.println("Skipping booking with missing user or room. Booking ID: " + booking.getId());
			}
		}

		return responses;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<BookingResponse> listAllBooking(Pageable pageable) {
		return bookingRepository.findAll(pageable).map(BookingMapper::toDTO);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<BookingResponse> listAllBookingByHotel(Long hotelId, Pageable pageable) {
		return bookingRepository.findAllByHotelId(hotelId, pageable).map(BookingMapper::toDTO);
	}

	@Override
	public boolean updateBookingStatus(Long bookingId, String newStatus) {
	    Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
	    if (optionalBooking.isEmpty()) {
	        throw new ResourceNotFoundException("Booking not found with ID: " + bookingId);
	    }

	    Booking booking = optionalBooking.get();

	    // Normalize and validate status input
	    String normalizedStatus = newStatus.trim().toUpperCase();

	    // Optional: Define allowed status values (or compare to BookingStatus enum values)
	    Set<String> allowedStatuses = Set.of("PENDING", "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED");

	    if (!allowedStatuses.contains(normalizedStatus)) {
	        throw new IllegalArgumentException("Invalid booking status: " + newStatus);
	    }


	    // Update status
	    booking.setStatus(BookingStatus.valueOf(normalizedStatus));
	    bookingRepository.save(booking);
	    return true;
	}
	
	public void deleteBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        
        bookingRepository.delete(booking);
    }
	
//	private void generatePasscodeAfterPayment(Long bookingId) {
//	    Booking booking = bookingRepository.findByIdAndPaidTrue(bookingId)
//	        .orElseThrow(() -> new RuntimeException("Booking not found or unpaid"));
//
//	    String passcode = generateSecurePasscode();
//	    booking.setCheckInPasscode(passcode);
//	    booking.setPasscodeGeneratedAt(LocalDateTime.now());
//
//	    bookingRepository.save(booking);
//
//	    emailService.sendPasscodeEmail(booking.getGuestEmail(), passcode);
//	}
//
//	private String generateSecurePasscode() {
//	    int length = 6;
//	    String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // avoid 0O1l
//	    SecureRandom random = new SecureRandom();
//	    StringBuilder sb = new StringBuilder();
//
//	    for (int i = 0; i < length; i++) {
//	        sb.append(chars.charAt(random.nextInt(chars.length())));
//	    }
//
//	    return sb.toString();
//	}


}
