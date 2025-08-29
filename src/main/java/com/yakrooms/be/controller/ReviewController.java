package com.yakrooms.be.controller;

import com.yakrooms.be.dto.request.ReviewRequest;
import com.yakrooms.be.dto.response.ReviewResponse;
import com.yakrooms.be.exception.ResourceConflictException;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
	@Autowired
	private ReviewService reviewService;

	// Create a new review - Only GUEST can create reviews
	@PreAuthorize("hasRole('GUEST')")
	@PostMapping
	public ResponseEntity<?> createReview(@RequestBody ReviewRequest reviewRequest) {
		try {
			ReviewResponse response = reviewService.createReview(reviewRequest);
			return ResponseEntity.ok(response);
		} catch (ResourceConflictException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create review.");
		}
	}

	// Submit rating - Only GUEST can submit ratings
	@PreAuthorize("hasRole('GUEST')")
	@PostMapping("/rating")
	public ResponseEntity<?> submitRating(@RequestBody Map<String, Object> ratingRequest) {
		try {
			// TODO: Implement rating submission logic
			// This could include storing ratings in database, calculating averages, etc.
			
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Rating submitted successfully!");
			response.put("timestamp", System.currentTimeMillis());
			
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", "Failed to submit rating. Please try again.");
			errorResponse.put("error", e.getMessage());
			
			return ResponseEntity.internalServerError().body(errorResponse);
		}
	}

	// Get average rating across platform - Public access
	@GetMapping("/averageRating")
	public ResponseEntity<?> getAverageRating() {
		try {
			// TODO: Implement average rating calculation across all hotels
			double avgRating = 4.5; // Placeholder value
			return ResponseEntity.ok(avgRating);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve average rating.");
		}
	}

	// Get average rating for a hotel - Public access
	@GetMapping("/hotel/{hotelId}/average-rating")
	public ResponseEntity<?> getAverageRatingForHotel(@PathVariable Long hotelId) {
		try {
			double avg = reviewService.getAverageRatingForHotel(hotelId);
			return ResponseEntity.ok(avg);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve average rating.");
		}
	}

	// Get number of reviews for a hotel - Public access
	@GetMapping("/hotel/{hotelId}/review-count")
	public ResponseEntity<?> getReviewCountForHotel(@PathVariable Long hotelId) {
		try {
			long count = reviewService.getReviewCountForHotel(hotelId);
			return ResponseEntity.ok(count);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve review count.");
		}
	}

	// Get paginated reviews for a hotel (for testimonials with pagination) - Public access
	@GetMapping("/hotel/{hotelId}/testimonials/paginated")
	public ResponseEntity<?> getReviewsForHotelPaginated(@PathVariable Long hotelId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		try {
			Pageable pageable = PageRequest.of(page, size);
			Page<ReviewResponse> reviewsPage = reviewService.getReviewsForHotelPaginated(hotelId, pageable);
			return ResponseEntity.ok(reviewsPage);
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to retrieve reviews for hotel.");
		}
	}
}
