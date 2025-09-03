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
import com.yakrooms.be.dto.request.BookingExtensionRequest;
import com.yakrooms.be.dto.response.BookingResponse;
import com.yakrooms.be.dto.response.BookingExtensionResponse;
import com.yakrooms.be.dto.response.CancellationRequestResponse;
import com.yakrooms.be.service.BookingService;
import com.yakrooms.be.service.UnifiedBookingService;
import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.repository.RoomRepository;
import com.yakrooms.be.exception.ResourceNotFoundException;
import org.springframework.format.annotation.DateTimeFormat;


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
	//Not used anywhere 
	// Create a single-night booking (checkout date auto-set to next day) - GUEST, HOTEL_ADMIN, and STAFF can create
	@PreAuthorize("hasAnyRole('GUEST', 'HOTEL_ADMIN', 'STAFF')")
	@PostMapping("/single-night")
	public ResponseEntity<BookingResponse> createSingleNightBooking(
			@Valid @RequestBody BookingRequest request) {
		
		try {
			logger.info("Creating single-night booking for room: {} with auto-checkout", request.getRoomId());
			
			// Validate that check-in date is provided
			if (request.getCheckInDate() == null) {
				logger.error("Check-in date is required for single-night booking");
				throw new IllegalArgumentException("Check-in date is required for single-night booking");
			}
			
			// Log the auto-checkout behavior for transparency
			LocalDate autoCheckoutDate = request.getCheckInDate().plusDays(1);
			logger.info("Auto-setting checkout date to: {} for check-in: {}", 
					   autoCheckoutDate, request.getCheckInDate());
			
			BookingResponse response = unifiedBookingService.createSingleNightBooking(request);
			
			logger.info("Successfully created single-night booking with ID: {}", response.getId());
			return ResponseEntity.ok(response);
			
		} catch (IllegalArgumentException e) {
			logger.error("Invalid request for single-night booking: {}", e.getMessage());
			throw new RuntimeException("Invalid request: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Failed to create single-night booking: {}", e.getMessage());
			throw new RuntimeException("Failed to create single-night booking: " + e.getMessage());
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
	
	// Request booking cancellation - GUEST can request cancellation (creates notification for staff)
	@PreAuthorize("hasAnyRole('GUEST', 'HOTEL_ADMIN', 'STAFF')")
	@PostMapping("/{bookingId}/request-cancellation")
	public ResponseEntity<Map<String, Object>> requestBookingCancellation(
			@PathVariable Long bookingId,
			@RequestParam Long userId) {
		
		try {
			logger.info("Requesting cancellation for booking: {} by user: {}", bookingId, userId);
			
			boolean success = unifiedBookingService.requestBookingCancellation(bookingId, userId);
			
			Map<String, Object> response = new HashMap<>();
			response.put("success", success);
			response.put("message", success ? 
				"Cancellation request submitted successfully. Staff will review and process your request." : 
				"Failed to submit cancellation request.");
			response.put("bookingId", bookingId);
			
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			logger.error("Failed to request cancellation for booking {}: {}", bookingId, e.getMessage());
			
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", "Failed to submit cancellation request: " + e.getMessage());
			errorResponse.put("bookingId", bookingId);
			
			return ResponseEntity.badRequest().body(errorResponse);
		}
	}
	
	// Extend booking stay - GUEST, HOTEL_ADMIN, and STAFF can extend
	@PreAuthorize("hasAnyRole('GUEST', 'HOTEL_ADMIN', 'STAFF')")
	@PutMapping("/{bookingId}/extend")
	public ResponseEntity<BookingExtensionResponse> extendBooking(
			@PathVariable Long bookingId,
			@Valid @RequestBody BookingExtensionRequest request) {
		
		try {
			logger.info("Extending booking: {} with request: {}", bookingId, request);
			
			BookingExtensionResponse response = unifiedBookingService.extendBooking(bookingId, request);
			
			if (response.isSuccess()) {
				return ResponseEntity.ok(response);
			} else {
				return ResponseEntity.badRequest().body(response);
			}
			
		} catch (Exception e) {
			logger.error("Failed to extend booking: {} - {}", bookingId, e.getMessage());
			BookingExtensionResponse errorResponse = new BookingExtensionResponse(
				bookingId, "Failed to extend booking: " + e.getMessage(), false);
			return ResponseEntity.badRequest().body(errorResponse);
		}
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

    // ========== SEARCH ENDPOINTS ==========
    
    /**
     * Search bookings by CID - Only HOTEL_ADMIN and STAFF can search
     */
    @PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
    @GetMapping("/search/cid")
    public ResponseEntity<Page<BookingResponse>> searchBookingsByCid(
            @RequestParam String cid,
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<BookingResponse> results = bookingService.searchBookingsByCid(cid, hotelId, pageable);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Failed to search bookings by CID: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Search bookings by phone number (exact match) - Only HOTEL_ADMIN and STAFF can search
     */
    @PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
    @GetMapping("/search/phone")
    public ResponseEntity<Page<BookingResponse>> searchBookingsByPhone(
            @RequestParam String phone,
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<BookingResponse> results = bookingService.searchBookingsByPhone(phone, hotelId, pageable);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Failed to search bookings by phone: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Search bookings by check-in date - Only HOTEL_ADMIN and STAFF can search
     */
    @PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
    @GetMapping("/search/checkin-date")
    public ResponseEntity<Page<BookingResponse>> searchBookingsByCheckInDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<BookingResponse> results = bookingService.searchBookingsByCheckInDate(checkInDate, hotelId, pageable);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Failed to search bookings by check-in date: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Search bookings by check-out date - Only HOTEL_ADMIN and STAFF can search
     */
    @PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
    @GetMapping("/search/checkout-date")
    public ResponseEntity<Page<BookingResponse>> searchBookingsByCheckOutDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<BookingResponse> results = bookingService.searchBookingsByCheckOutDate(checkOutDate, hotelId, pageable);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Failed to search bookings by check-out date: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Search bookings by status - Only HOTEL_ADMIN and STAFF can search
     */
    @PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
    @GetMapping("/search/status")
    public ResponseEntity<Page<BookingResponse>> searchBookingsByStatus(
            @RequestParam String status,
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<BookingResponse> results = bookingService.searchBookingsByStatus(status, hotelId, pageable);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Failed to search bookings by status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Search bookings by date range - Only HOTEL_ADMIN and STAFF can search
     */
    @PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
    @GetMapping("/search/date-range")
    public ResponseEntity<Page<BookingResponse>> searchBookingsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<BookingResponse> results = bookingService.searchBookingsByDateRange(startDate, endDate, hotelId, pageable);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Failed to search bookings by date range: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get cancellation requests for a specific hotel - Only HOTEL_ADMIN and STAFF can access
     */
    @PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
    @GetMapping("/cancellation-requests/hotel/{hotelId}")
    public ResponseEntity<?> getCancellationRequestsByHotel(
            @PathVariable Long hotelId) {
        try {
            logger.info("Fetching cancellation requests for hotel: {}", hotelId);
            List<CancellationRequestResponse> cancellationRequests = unifiedBookingService.getCancellationRequestsByHotel(hotelId);
            logger.info("Successfully fetched {} cancellation requests for hotel {}", cancellationRequests.size(), hotelId);
            return ResponseEntity.ok(cancellationRequests);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for hotel {}: {}", hotelId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to fetch cancellation requests for hotel {}: {}", hotelId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to fetch cancellation requests: " + e.getMessage()));
        }
    }
    


}
