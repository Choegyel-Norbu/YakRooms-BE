package com.yakrooms.be.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yakrooms.be.dto.request.BookingRequest;
import com.yakrooms.be.dto.response.BookingResponse;
import com.yakrooms.be.service.BookingService;


@RestController
@RequestMapping("/api/bookings")
public class BookingController {

	@Autowired
	private BookingService bookingService;

	// Create a booking (User)
	@PostMapping
	public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request) {
		BookingResponse response = bookingService.createBooking(request);
		return ResponseEntity.ok(response);
	}

	// Cancel booking (User)
	@PostMapping("/{id}/cancel")
	public ResponseEntity<Void> cancelBooking(@PathVariable("id") Long bookingId, @RequestParam("userId") Long userId) {
		bookingService.cancelBooking(bookingId, userId);
		return ResponseEntity.ok().build();
	}

	// Confirm booking (Admin/Owner)
	@PostMapping("/{id}/confirm")
	public ResponseEntity<BookingResponse> confirmBooking(@PathVariable("id") Long bookingId) {
		BookingResponse response = bookingService.confirmBooking(bookingId);
		return ResponseEntity.ok(response);
	}

	// Check room availability
	@GetMapping("/availability")
	public ResponseEntity<Boolean> checkRoomAvailability(@RequestParam Long roomId, @RequestParam LocalDate checkIn,
			@RequestParam LocalDate checkOut) {
		boolean isAvailable = bookingService.isRoomAvailable(roomId, checkIn, checkOut);
		return ResponseEntity.ok(isAvailable);
	}
	
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
	
	  @DeleteMapping("/{bookingId}")
	    public ResponseEntity<?> deleteBooking(@PathVariable Long bookingId) {
	        bookingService.deleteBookingById(bookingId);
	        return ResponseEntity.ok().body("Booking deleted successfully");
	    }

	// Get all bookings for a user
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable Long userId) {
		return ResponseEntity.ok(bookingService.getAllBookingsByUserId(userId));
	}

	// Get all bookings for a user with pagination
	@GetMapping("/user/{userId}/page")
	public ResponseEntity<Page<BookingResponse>> getUserBookingsPaginated(
			@PathVariable Long userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		return ResponseEntity.ok(bookingService.getAllBookingsByUserId(userId, pageable));
	}

	// Get all bookings for a user by status
	@GetMapping("/user/{userId}/status/{status}")
	public ResponseEntity<List<BookingResponse>> getUserBookingsByStatus(
			@PathVariable Long userId,
			@PathVariable String status) {
		return ResponseEntity.ok(bookingService.getAllBookingsByUserIdAndStatus(userId, status));
	}

	// Get all bookings for a hotel (Owner/Admin)
	@GetMapping("/hotel/{hotelId}")
	public ResponseEntity<List<BookingResponse>> getBookingsByHotel(@PathVariable Long hotelId) {
		return ResponseEntity.ok(bookingService.getBookingsByHotel(hotelId));
	}

	// Get a single booking detail
	@GetMapping("/{id}")
	public ResponseEntity<BookingResponse> getBookingDetails(@PathVariable("id") Long bookingId) {
		return ResponseEntity.ok(bookingService.getBookingDetails(bookingId));
	}
}

// NOTE:
// Authentication context (userId) is passed manually here for simplicity.
// In production, extract userId from security context or token.
