package com.yakrooms.be.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import com.yakrooms.be.dto.HotelListingDto;
import com.yakrooms.be.dto.request.HotelRequest;
import com.yakrooms.be.dto.response.HotelResponse;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.projection.HotelListingProjection;
import com.yakrooms.be.projection.HotelWithLowestPriceProjection;
import com.yakrooms.be.projection.HotelWithPriceProjection;
import com.yakrooms.be.service.HotelService;
import com.yakrooms.be.util.HotelSearchCriteria;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

	@Autowired
	private HotelService hotelService;

	// Create new hotel
	@PostMapping("/{userId}")
	public ResponseEntity<HotelResponse> createHotel(@RequestBody HotelRequest request, @PathVariable Long userId) {
		HotelResponse response = hotelService.createHotel(request, userId);
		return ResponseEntity.ok(response);
	}

	// Get hotel by ID
	@GetMapping("/{userId}")
	public ResponseEntity<HotelListingDto> getHotelById(@PathVariable Long userId) {
		return ResponseEntity.ok(hotelService.getListingForUser(userId));
	}

	@GetMapping("/details/{hotelId}")
	public ResponseEntity<HotelResponse> getHotelByHotelId(@PathVariable Long hotelId) {
		return ResponseEntity.ok(hotelService.getHotelById(hotelId));
	}

	// Get all hotels with pagination
	@GetMapping
	public ResponseEntity<Page<HotelWithLowestPriceProjection>> getAllHotels(@PageableDefault(size = 10) Pageable pageable) {
		return ResponseEntity.ok(hotelService.getAllHotels(pageable));
	}
	
	@GetMapping("/superAdmin")
	public ResponseEntity<Page<HotelResponse>> getAllHotelsForSuperAdmin(@PageableDefault(size = 10) Pageable pageable) {
		Page<HotelResponse> hotels = hotelService.getAllHotelsForSuperAdmin(pageable);
		return ResponseEntity.ok(hotels);
	}

	@GetMapping("/sortedByLowestPrice")
	public ResponseEntity<Page<HotelWithLowestPriceProjection>> getAllHotelsSortedByLowestPrice(@PageableDefault(size = 10) Pageable pageable) {
		return ResponseEntity.ok(hotelService.getAllHotelsSortedByLowestPrice(pageable));
	}

	@GetMapping("/sortedByHighestPrice")
	public ResponseEntity<Page<HotelWithLowestPriceProjection>> getAllHotelsSortedByHighestPrice(@PageableDefault(size = 10) Pageable pageable) {
		return ResponseEntity.ok(hotelService.getAllHotelsSortedByHighestPrice(pageable));
	}

	// Update a hotel by ID
	@PutMapping("/{id}")
	public ResponseEntity<HotelResponse> updateHotel(@PathVariable Long id, @RequestBody HotelRequest request) {
		return ResponseEntity.ok(hotelService.updateHotel(id, request));
	}

	@GetMapping("/search")
	public ResponseEntity<Page<HotelResponse>> searchHotels(@RequestParam(required = false) String district,
			@RequestParam(required = false) String hotelType, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		Page<HotelResponse> results = hotelService.searchHotels(district, hotelType, page, size);
		return ResponseEntity.ok(results);
	}

	@GetMapping("/topThree")
	public ResponseEntity<List<HotelWithPriceProjection>> getTopThreeHotels() {
		return ResponseEntity.ok(hotelService.getTopThreeHotels());
	}

	// Delete a hotel by ID
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
		hotelService.deleteHotel(id);
		return ResponseEntity.noContent().build();
	}

	// Verify hotel listing (Admin/Moderator)
	@PostMapping("/{id}/verify")
	public ResponseEntity<Void> verifyHotel(@PathVariable Long id) {
		hotelService.verifyHotel(id);
		return ResponseEntity.ok().build();
	}

	// Search hotels by criteria
//	@PostMapping("/search")
//	public ResponseEntity<List<HotelResponse>> searchHotels(@RequestBody HotelSearchCriteria criteria) {
//		return ResponseEntity.ok(hotelService.searchHotels(criteria));
//	}
}