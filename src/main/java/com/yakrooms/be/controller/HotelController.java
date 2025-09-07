package com.yakrooms.be.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import com.yakrooms.be.dto.HotelListingDto;
import com.yakrooms.be.dto.request.HotelRequest;
import com.yakrooms.be.dto.response.HotelResponse;
import com.yakrooms.be.dto.response.PagedResponse;
import com.yakrooms.be.projection.HotelWithLowestPriceProjection;
import com.yakrooms.be.projection.HotelWithPriceProjection;
import com.yakrooms.be.service.HotelService;
import com.yakrooms.be.util.PageUtils;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

	@Autowired
	private HotelService hotelService;

	// Create new hotel - GUEST users can create hotels (promotes to HOTEL_ADMIN)
	@PreAuthorize("hasAnyRole('GUEST', 'HOTEL_ADMIN')")
	@PostMapping("/{userId}")
	public ResponseEntity<HotelResponse> createHotel(@RequestBody HotelRequest request, @PathVariable Long userId) {
		HotelResponse response = hotelService.createHotel(request, userId);
		return ResponseEntity.ok(response);
	}

	// Get hotel by user ID - HOTEL_ADMIN and STAFF can access their own hotel
	@PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF', 'GUEST')")
	@GetMapping("/{userId}")
	public ResponseEntity<HotelListingDto> getHotelById(@PathVariable Long userId) {
		return ResponseEntity.ok(hotelService.getListingForUser(userId));
	}

	// Get hotel details by hotel ID - Public access
	@PreAuthorize("permitAll()")
	@GetMapping("/details/{hotelId}")
	public ResponseEntity<HotelResponse> getHotelByHotelId(@PathVariable Long hotelId) {
		return ResponseEntity.ok(hotelService.getHotelById(hotelId));
	}

	// Get all hotels with pagination - Public access
	@PreAuthorize("permitAll()")
	@GetMapping("/list")
	public ResponseEntity<PagedResponse<HotelWithLowestPriceProjection>> getAllHotels(
			@PageableDefault(size = 10) Pageable pageable) {
		Page<HotelWithLowestPriceProjection> page = hotelService.getAllHotels(pageable);
		return ResponseEntity.ok(PageUtils.toPagedResponse(page));
	}

	// Get all hotels for super admin - Only SUPER_ADMIN can access
	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@GetMapping("/superAdmin")
	public ResponseEntity<PagedResponse<HotelResponse>> getAllHotelsForSuperAdmin(
			@PageableDefault(size = 10) Pageable pageable) {
		Page<HotelResponse> hotels = hotelService.getAllHotelsForSuperAdmin(pageable);
		return ResponseEntity.ok(PageUtils.toPagedResponse(hotels));
	}

	// Get hotels sorted by lowest price - Public access
	@PreAuthorize("permitAll()")
	@GetMapping("/sortedByLowestPrice")
	public ResponseEntity<PagedResponse<HotelWithLowestPriceProjection>> getAllHotelsSortedByLowestPrice(
			@PageableDefault(size = 10) Pageable pageable) {
		Page<HotelWithLowestPriceProjection> page = hotelService.getAllHotelsSortedByLowestPrice(pageable);
		return ResponseEntity.ok(PageUtils.toPagedResponse(page));
	}

	// Get hotels sorted by highest price - Public access
	@PreAuthorize("permitAll()")
	@GetMapping("/sortedByHighestPrice")
	public ResponseEntity<PagedResponse<HotelWithLowestPriceProjection>> getAllHotelsSortedByHighestPrice(
			@PageableDefault(size = 10) Pageable pageable) {
		Page<HotelWithLowestPriceProjection> page = hotelService.getAllHotelsSortedByHighestPrice(pageable);
		return ResponseEntity.ok(PageUtils.toPagedResponse(page));
	}

	// Update a hotel by ID - Only HOTEL_ADMIN can update
	@PreAuthorize("hasRole('HOTEL_ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<HotelResponse> updateHotel(@PathVariable Long id, @RequestBody HotelRequest request) {
		return ResponseEntity.ok(hotelService.updateHotel(id, request));
	}

	// Search hotels - Public access
	@PreAuthorize("permitAll()")
	@GetMapping("/search")
	public ResponseEntity<PagedResponse<HotelWithLowestPriceProjection>> searchHotels(
			@RequestParam(required = false) String district, 
			@RequestParam(required = false) String locality,
			@RequestParam(required = false) String hotelType,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		Page<HotelWithLowestPriceProjection> results = hotelService.searchHotels(district, locality, hotelType, page, size);
		return ResponseEntity.ok(PageUtils.toPagedResponse(results));
	}

	// Get top three hotels - Public access
	@GetMapping("/topThree")
	public ResponseEntity<List<HotelWithPriceProjection>> getTopThreeHotels() {
		return ResponseEntity.ok(hotelService.getTopThreeHotels());
	}

	// Delete a hotel by ID - Only HOTEL_ADMIN can delete
	@PreAuthorize("hasRole('HOTEL_ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
		hotelService.deleteHotel(id);
		return ResponseEntity.noContent().build();
	}

	// Verify hotel listing - Only SUPER_ADMIN can verify
	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@PostMapping("/{id}/verify")
	public ResponseEntity<Map<String, Object>> verifyHotel(@PathVariable Long id) {
		try {
			Map<String, Object> serviceResult = hotelService.verifyHotel(id);

			// Build response based on service result
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.putAll(serviceResult); // Include all service results

			// Add user-friendly message based on results
			boolean emailSent = (Boolean) serviceResult.getOrDefault("emailSent", false);
			boolean alreadyVerified = (Boolean) serviceResult.getOrDefault("alreadyVerified", false);

			if (alreadyVerified) {
				response.put("message", "Hotel was already verified");
			} else if (emailSent) {
				response.put("message", "Hotel verified successfully and notification email sent");
			} else {
				response.put("message", "Hotel verified successfully, but email notification failed");
			}

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("hotelId", id);
			response.put("message", "Failed to verify hotel: " + e.getMessage());
			response.put("emailSent", false);
			response.put("error", e.getMessage());

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
}