package com.yakrooms.be.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.yakrooms.be.dto.request.BookingRequest;
import com.yakrooms.be.dto.response.BookingResponse;
import com.yakrooms.be.service.BookingService;
import com.yakrooms.be.service.UnifiedBookingService;
import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.repository.RoomRepository;
import com.yakrooms.be.exception.ResourceNotFoundException;


@RestController
@RequestMapping("/api/bookings")
public class BookingController {

	private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

	@Autowired
	private BookingService bookingService;
	
	@Autowired
	private RoomRepository roomRepository;
	
	// New unified booking service
	@Autowired
	private UnifiedBookingService unifiedBookingService;

	
	// Create a booking - GUEST, HOTEL_ADMIN, and STAFF can create
	@PreAuthorize("hasAnyRole('GUEST', 'HOTEL_ADMIN', 'STAFF')")
	@PostMapping
	public ResponseEntity<BookingResponse> createBooking(
			@Valid @RequestBody BookingRequest request) {
		
		try {
			BookingResponse response = unifiedBookingService.createBooking(request);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			logger.error("Failed to create booking: {}", e.getMessage());
			throw new RuntimeException("Failed to create booking: " + e.getMessage());
		}
	}
	
	// Get all bookings for hotel - Only HOTEL_ADMIN and STAFF can access
	@PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
	@GetMapping("/")
	public Page<BookingResponse> getAllBookings(
	    @RequestParam(required = false) Long hotelId,
	    @RequestParam(defaultValue = "0") int page,
	    @RequestParam(defaultValue = "10") int size) {
	    
	    Pageable pageable = PageRequest.of(page, size);
	    if (hotelId != null) {
	        return bookingService.listAllBookingByHotel(hotelId, pageable);
	    }
	    return bookingService.listAllBooking(pageable);
	}

	// Update booking status - Only HOTEL_ADMIN and STAFF can update
	@PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
	@PutMapping("/{bookingId}/status/{status}")
	public ResponseEntity<String> updateBookingStatus(
	        @PathVariable Long bookingId,
	        @PathVariable String status) {

        boolean success = bookingService.updateBookingStatus(bookingId, status);

        if (success) {
            return ResponseEntity.ok("Booking status updated successfully.");
        } else {
            return ResponseEntity.badRequest().body("Failed to update booking status.");
        }
    }
	
	// Check-in guest - Only HOTEL_ADMIN and STAFF can check-in guests
	@PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
	@PutMapping("/{bookingId}/status/checked_in")
	public ResponseEntity<String> checkInGuest(@PathVariable Long bookingId) {
		try {
			boolean success = bookingService.updateBookingStatus(bookingId, "CHECKED_IN");
			if (success) {
				return ResponseEntity.ok("Guest checked in successfully.");
			} else {
				return ResponseEntity.badRequest().body("Failed to check-in guest.");
			}
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body("Error during check-in: " + e.getMessage());
		}
	}
	
	// Delete booking - GUEST, HOTEL_ADMIN, and STAFF can delete
	@PreAuthorize("hasAnyRole('GUEST', 'HOTEL_ADMIN', 'STAFF')")
	@DeleteMapping("/{bookingId}")
	public ResponseEntity<?> deleteBooking(@PathVariable Long bookingId) {
		bookingService.deleteBookingById(bookingId);
		return ResponseEntity.ok().body("Booking deleted successfully");
	}

	// Get all bookings for a user - Only GUEST can access their own bookings
	@PreAuthorize("hasRole('GUEST')")
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable Long userId) {
		return ResponseEntity.ok(bookingService.getAllBookingsByUserId(userId));
	}

	// Get all bookings for a user with pagination - Only GUEST can access their own bookings
	@PreAuthorize("hasRole('GUEST')")
	@GetMapping("/user/{userId}/page")
	public ResponseEntity<Page<BookingResponse>> getUserBookingsPaginated(
			@PathVariable Long userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		return ResponseEntity.ok(bookingService.getAllBookingsByUserId(userId, pageable));
	}

	// Get all bookings for a user by status - Only GUEST can access their own bookings
	@PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
	@GetMapping("/user/{userId}/status/{status}")
	public ResponseEntity<List<BookingResponse>> getUserBookingsByStatus(
			@PathVariable Long userId,
			@PathVariable String status) {
		return ResponseEntity.ok(bookingService.getAllBookingsByUserIdAndStatus(userId, status));
	}

	// Get all bookings for a hotel - Only HOTEL_ADMIN and STAFF can access
	@PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
	@GetMapping("/hotel/{hotelId}")
	public ResponseEntity<List<BookingResponse>> getBookingsByHotel(@PathVariable Long hotelId) {
		return ResponseEntity.ok(bookingService.getBookingsByHotel(hotelId));
	}

	// Get a single booking detail - HOTEL_ADMIN, STAFF, and GUEST can access
	@PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF', 'GUEST')")
	@GetMapping("/{id}")
	public ResponseEntity<BookingResponse> getBookingDetails(@PathVariable("id") Long bookingId) {
		return ResponseEntity.ok(bookingService.getBookingDetails(bookingId));
	}

	// Debug endpoint to check room capacity - Only HOTEL_ADMIN and STAFF can access
	@PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
	@GetMapping("/debug/room/{roomId}/capacity")
	public ResponseEntity<Map<String, Object>> getRoomCapacityInfo(@PathVariable Long roomId) {
		try {
			Room room = roomRepository.findById(roomId)
				.orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
			
			Map<String, Object> response = new HashMap<>();
			response.put("roomId", room.getId());
			response.put("roomNumber", room.getRoomNumber());
			response.put("maxGuests", room.getMaxGuests());
			response.put("roomType", room.getRoomType());
			response.put("isAvailable", room.isAvailable());
			response.put("hotelName", room.getHotel().getName());
			
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			Map<String, Object> error = new HashMap<>();
			error.put("error", e.getMessage());
			return ResponseEntity.badRequest().body(error);
		}
	}
}
