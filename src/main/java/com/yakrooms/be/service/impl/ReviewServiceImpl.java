package com.yakrooms.be.service.impl;

import com.yakrooms.be.model.entity.Review;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.repository.ReviewRepository;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.repository.BookingRepository;
import com.yakrooms.be.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @Override
    @Transactional
    public Review createReview(Long hotelId, Long userId, int rating, String comment) {
        if (!canUserReviewHotel(hotelId, userId)) {
            throw new IllegalArgumentException("User is not eligible to review this hotel.");
        }
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("Hotel not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Review review = new Review();
        review.setHotel(hotel);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public Review updateReview(Long reviewId, Long userId, int rating, String comment) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        if (!review.getUser().getId().equals(userId)) {
            throw new SecurityException("User is not authorized to update this review.");
        }
        review.setRating(rating);
        review.setComment(comment);
        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        if (!review.getUser().getId().equals(userId)) {
            throw new SecurityException("User is not authorized to delete this review.");
        }
        reviewRepository.delete(review);
    }

    @Override
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
    }

    @Override
    public List<Review> getReviewsByHotel(Long hotelId) {
        return reviewRepository.findByHotelId(hotelId);
    }

    @Override
    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    @Override
    public boolean canUserReviewHotel(Long hotelId, Long userId) {
        // User can review if they have a completed booking for the hotel and haven't already reviewed
        List<Booking> bookings = bookingRepository.findByHotelIdAndUserIdAndStatus(hotelId, userId, "CHEC");
        if (bookings.isEmpty()) return false;
        List<Review> existingReviews = reviewRepository.findByHotelId(hotelId);
        return existingReviews.stream().noneMatch(r -> r.getUser().getId().equals(userId));
    }

    @Override
    public double getAverageRatingForHotel(Long hotelId) {
        List<Review> reviews = reviewRepository.findByHotelId(hotelId);
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }

    @Override
    public long getReviewCountForHotel(Long hotelId) {
        return reviewRepository.countByHotelId(hotelId);
    }
} 