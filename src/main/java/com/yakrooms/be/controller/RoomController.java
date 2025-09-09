package com.yakrooms.be.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yakrooms.be.dto.RoomResponseDTO;
import com.yakrooms.be.dto.RoomBookedDatesDTO;
import com.yakrooms.be.dto.RoomStatusDTO;
import com.yakrooms.be.dto.request.RoomRequest;
import com.yakrooms.be.dto.response.PagedResponse;
import com.yakrooms.be.service.RoomService;
import com.yakrooms.be.service.RoomAvailabilityScheduler;
import com.yakrooms.be.util.PageUtils;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

	@Autowired
	private RoomService roomService;
	
	@Autowired
	private RoomAvailabilityScheduler roomAvailabilityScheduler;

	// Create new room - Only HOTEL_ADMIN and STAFF can create
	@PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF', 'GUEST')")
	@PostMapping("/hotel/{hotelId}")
	public ResponseEntity<?> createRoom(@PathVariable Long hotelId, @RequestBody RoomRequest request) {
		return ResponseEntity.ok(roomService.createRoom(hotelId, request));
	}

	// Get room by ID - Public access
	@GetMapping("/{roomId}")
	public ResponseEntity<RoomResponseDTO> getRoomById(@PathVariable Long roomId) {
		return ResponseEntity.ok(roomService.getRoomById(roomId));
	}

	// Get all rooms for a specific hotel - Only HOTEL_ADMIN and STAFF can access
	@PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
	@GetMapping("/hotel/{hotelId}")
	public ResponseEntity<List<RoomResponseDTO>> getRoomsByHotel(@PathVariable Long hotelId) {
		return ResponseEntity.ok(roomService.getRoomsByHotel(hotelId));
	}

	// Test endpoint to verify security configuration
	@GetMapping("/test")
	public ResponseEntity<String> testEndpoint() {
		return ResponseEntity.ok("Room test endpoint is working!");
	}

	// Get available rooms for booking - Public access
	@GetMapping("/available/{hotelId}")
	public ResponseEntity<PagedResponse<RoomResponseDTO>> getAvailable(@PathVariable Long hotelId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<RoomResponseDTO> roomPage = roomService.getAvailableRooms(hotelId, pageable);
		return ResponseEntity.ok(PageUtils.toPagedResponse(roomPage));
	}
	
	// Get booked dates for a room - Public access (no authentication required)
	@GetMapping("/{roomId}/booked-dates")
	public ResponseEntity<RoomBookedDatesDTO> getBookedDatesForRoom(@PathVariable Long roomId) {
		RoomBookedDatesDTO bookedDates = roomService.getBookedDatesForRoom(roomId);
		return ResponseEntity.ok(bookedDates);
	}

	// Update room information - Only HOTEL_ADMIN and STAFF can update
	@PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
	@PutMapping("/{roomId}")
	public ResponseEntity<RoomResponseDTO> updateRoom(@PathVariable Long roomId, @RequestBody RoomRequest request) {
		return ResponseEntity.ok(roomService.updateRoom(roomId, request));
	}

	// Delete room - Only HOTEL_ADMIN and STAFF can delete
	@PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
	@DeleteMapping("/{roomId}")
	public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
		roomService.deleteRoom(roomId);
		return ResponseEntity.noContent().build();
	}

	// Toggle room availability - Only HOTEL_ADMIN and STAFF can toggle
	@PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
	@PatchMapping("/{roomId}/availability")
	public ResponseEntity<RoomResponseDTO> toggleAvailability(@PathVariable Long roomId,
			@RequestParam boolean isAvailable) {
		return ResponseEntity.ok(roomService.toggleAvailability(roomId, isAvailable));
	}

	// Get room status for hotel management - Only HOTEL_ADMIN and STAFF can access
	@PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
	@GetMapping("/status/{hotelId}")
	public ResponseEntity<PagedResponse<RoomStatusDTO>> getRoomStatus(@PathVariable Long hotelId, Pageable pageable) {
		Page<RoomStatusDTO> statusPage = roomService.getRoomStatusByHotelId(hotelId, pageable);
		return ResponseEntity.ok(PageUtils.toPagedResponse(statusPage));
	}

	// Search rooms by room number - Only HOTEL_ADMIN and STAFF can access
	@PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
	@GetMapping("/status/{hotelId}/search")
	public ResponseEntity<PagedResponse<RoomStatusDTO>> getRoomStatusByRoomNumber(
			@PathVariable Long hotelId,
			@RequestParam String roomNumber,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<RoomStatusDTO> statusPage = roomService.getRoomStatusByHotelIdAndRoomNumber(hotelId, roomNumber, pageable);
		return ResponseEntity.ok(PageUtils.toPagedResponse(statusPage));
	}
	
	// Manual trigger for room availability scheduler - Only HOTEL_ADMIN and STAFF can access
	@PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
	@PostMapping("/scheduler/trigger")
	public ResponseEntity<String> triggerRoomAvailabilityScheduler() {
		try {
			roomAvailabilityScheduler.manualRoomAvailabilityUpdate();
			return ResponseEntity.ok("Room availability scheduler triggered successfully. Check logs for details.");
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
				.body("Failed to trigger room availability scheduler: " + e.getMessage());
		}
	}
}