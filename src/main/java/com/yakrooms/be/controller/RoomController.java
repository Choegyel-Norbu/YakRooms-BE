package com.yakrooms.be.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
import com.yakrooms.be.dto.request.RoomRequest;
import com.yakrooms.be.projection.RoomStatusProjection;
import com.yakrooms.be.service.RoomService;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

	@Autowired
	private RoomService roomService;

	@PostMapping("/hotel/{hotelId}")
	public ResponseEntity<?> createRoom(@PathVariable Long hotelId, @RequestBody RoomRequest request) {
		return ResponseEntity.ok(roomService.createRoom(hotelId, request));
	}

	@GetMapping("/{roomId}")
	public ResponseEntity<RoomResponseDTO> getRoomById(@PathVariable Long roomId) {
		return ResponseEntity.ok(roomService.getRoomById(roomId));
	}

	@GetMapping("/hotel/{hotelId}")
	public ResponseEntity<List<RoomResponseDTO>> getRoomsByHotel(@PathVariable Long hotelId) {
		return ResponseEntity.ok(roomService.getRoomsByHotel(hotelId));
	}

	@GetMapping("/available/{hotelId}")
	public ResponseEntity<Page<RoomResponseDTO>> getAvailable(@PathVariable Long hotelId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		return ResponseEntity.ok(roomService.getAvailableRooms(hotelId, pageable));
	}

	@PutMapping("/{roomId}")
	public ResponseEntity<RoomResponseDTO> updateRoom(@PathVariable Long roomId, @RequestBody RoomRequest request) {
		return ResponseEntity.ok(roomService.updateRoom(roomId, request));
	}

	@DeleteMapping("/{roomId}")
	public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
		roomService.deleteRoom(roomId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{roomId}/availability")
	public ResponseEntity<RoomResponseDTO> toggleAvailability(@PathVariable Long roomId,
			@RequestParam boolean isAvailable) {
		return ResponseEntity.ok(roomService.toggleAvailability(roomId, isAvailable));
	}

	@GetMapping("/status/{hotelId}")
	public ResponseEntity<Page<RoomStatusProjection>> getRoomStatus(@PathVariable Long hotelId, Pageable pageable) {
		return ResponseEntity.ok(roomService.getRoomStatusByHotelId(hotelId, pageable));
	}

	@GetMapping("/status/{hotelId}/search")
	public ResponseEntity<Page<RoomStatusProjection>> getRoomStatusByRoomNumber(
			@PathVariable Long hotelId,
			@RequestParam String roomNumber,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		return ResponseEntity.ok(roomService.getRoomStatusByHotelIdAndRoomNumber(hotelId, roomNumber, pageable));
	}
	
	
	
	
	
	
	
}