package com.yakrooms.be.controller;

import com.yakrooms.be.dto.request.ReviewRequest;
import com.yakrooms.be.dto.response.ReviewResponse;
import com.yakrooms.be.exception.ResourceConflictException;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    // Create a new review
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest reviewRequest
                                         ) {
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

   

    // Get average rating for a hotel
    @GetMapping("/hotel/{hotelId}/average-rating")
    public ResponseEntity<?> getAverageRatingForHotel(@PathVariable Long hotelId) {
        try {
            double avg = reviewService.getAverageRatingForHotel(hotelId);
            return ResponseEntity.ok(avg);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve average rating.");
        }
    }

    // Get number of reviews for a hotel
    @GetMapping("/hotel/{hotelId}/review-count")
    public ResponseEntity<?> getReviewCountForHotel(@PathVariable Long hotelId) {
        try {
            long count = reviewService.getReviewCountForHotel(hotelId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve review count.");
        }
    }

    // Get all reviews for a hotel (for testimonials)
    @GetMapping("/hotel/{hotelId}/testimonials")
    public ResponseEntity<?> getAllReviewsForHotel(@PathVariable Long hotelId) {
        try {
            List<ReviewResponse> reviews = reviewService.getAllReviewsForHotel(hotelId);
            return ResponseEntity.ok(reviews);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve reviews for hotel.");
        }
    }

    // Get paginated reviews for a hotel (for testimonials with pagination)
    @GetMapping("/hotel/{hotelId}/testimonials/paginated")
    public ResponseEntity<?> getReviewsForHotelPaginated(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
    	Pageable pageable = PageRequest.of(page, size);
    	Page<ReviewResponse> reviewsPagee = reviewService.getReviewsForHotelPaginated(hotelId, pageable);
        String check = null;

        try {
            
            Page<ReviewResponse> reviewsPage = reviewService.getReviewsForHotelPaginated(hotelId, pageable);
            return ResponseEntity.ok(reviewsPage);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve reviews for hotel.");
        }
    }
} 