package com.yakrooms.be.controller;

import com.yakrooms.be.model.entity.Review;
import com.yakrooms.be.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    // Create a new review
    @PostMapping
    public ResponseEntity<?> createReview(@RequestParam Long hotelId,
                                          @RequestParam Long userId,
                                          @RequestParam int rating,
                                          @RequestParam(required = false) String comment) {
        try {
            Review review = reviewService.createReview(hotelId, userId, rating, comment);
            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create review.");
        }
    }

    // Update a review
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable Long reviewId,
                                          @RequestParam Long userId,
                                          @RequestParam int rating,
                                          @RequestParam(required = false) String comment) {
        try {
            Review review = reviewService.updateReview(reviewId, userId, rating, comment);
            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update review.");
        }
    }

    // Delete a review
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId,
                                          @RequestParam Long userId) {
        try {
            reviewService.deleteReview(reviewId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete review.");
        }
    }

    // Get a review by ID
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewById(@PathVariable Long reviewId) {
        try {
            Review review = reviewService.getReviewById(reviewId);
            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve review.");
        }
    }

    // Get all reviews for a hotel
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<?> getReviewsByHotel(@PathVariable Long hotelId) {
        try {
            List<Review> reviews = reviewService.getReviewsByHotel(hotelId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve reviews for hotel.");
        }
    }

    // Get all reviews by a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getReviewsByUser(@PathVariable Long userId) {
        try {
            List<Review> reviews = reviewService.getReviewsByUser(userId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve reviews for user.");
        }
    }

    // Check if user can review a hotel
    @GetMapping("/can-review")
    public ResponseEntity<?> canUserReviewHotel(@RequestParam Long hotelId,
                                                @RequestParam Long userId) {
        try {
            boolean canReview = reviewService.canUserReviewHotel(hotelId, userId);
            return ResponseEntity.ok(canReview);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to check review eligibility.");
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
} 