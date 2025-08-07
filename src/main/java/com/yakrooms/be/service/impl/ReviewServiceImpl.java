package com.yakrooms.be.service.impl;

import com.yakrooms.be.dto.request.ReviewRequest;
import com.yakrooms.be.dto.response.ReviewResponse;
import com.yakrooms.be.exception.ResourceConflictException;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.Review;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.repository.ReviewRepository;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public double getAverageRatingForHotel(Long hotelId) {
        return reviewRepository.findAverageRatingByHotel(hotelId)
                .orElse(0.0);
    }

    @Override
    @Transactional(readOnly = true)
    public long getReviewCountForHotel(Long hotelId) {
        return reviewRepository.countReviewsByHotel(hotelId);
    }

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest reviewRequest) {
        // Validate input
        if (reviewRequest.rating < 1 || reviewRequest.rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        if (reviewRequest.hotelId == null) {
            throw new IllegalArgumentException("Hotel ID is required");
        }

        // Find hotel
        Hotel hotel = hotelRepository.findById(reviewRequest.hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + reviewRequest.hotelId));

        // Find user
        User user = userRepository.findById(reviewRequest.userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + reviewRequest.userId));

        // Check if user has already reviewed this hotel
        boolean existingReview = reviewRepository.existsByHotelAndUser(hotel, user);
        if (existingReview) {
            throw new ResourceConflictException("You have already reviewed this hotel");
        }

        // Create new review
        Review review = new Review();
        review.setRating(reviewRequest.rating);
        review.setComment(reviewRequest.comment);
        review.setHotel(hotel);
        review.setUser(user);

        // Save review
        Review savedReview = reviewRepository.save(review);

        // Create response using helper method
        return convertToReviewResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getAllReviewsForHotel(Long hotelId) {
        // Validate hotel exists
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel not found with id: " + hotelId);
        }

        // Get all reviews for the hotel
        List<Review> reviews = reviewRepository.findAllByHotelIdOrderByCreatedAtDesc(hotelId);

        // Convert to DTOs
        return reviews.stream()
                .map(this::convertToReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsForHotelPaginated(Long hotelId, Pageable pageable) {
        // Validate hotel exists
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel not found with id: " + hotelId);
        }

        // Get paginated reviews for the hotel using EntityGraph for optimal performance
        Page<Review> reviewsPage = reviewRepository.findByHotelIdOrderByCreatedAtDesc(hotelId, pageable);

        // Convert to DTOs
        return reviewsPage.map(this::convertToReviewResponse);
    }

    /**
     * Helper method to convert Review entity to ReviewResponse DTO
     */
    private ReviewResponse convertToReviewResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.id = review.getId();
        response.rating = review.getRating();
        response.comment = review.getComment();
        response.userId = review.getUser().getId();
        response.userName = review.getUser().getName();
        response.userEmail = review.getUser().getEmail();
        response.userProfilePicUrl = review.getUser().getProfilePicUrl();
        response.createdAt = review.getCreatedAt();
        return response;
    }
} 