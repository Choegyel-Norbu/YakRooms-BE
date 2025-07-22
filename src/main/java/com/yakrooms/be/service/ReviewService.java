package com.yakrooms.be.service;

import com.yakrooms.be.model.entity.Review;
import java.util.List;

public interface ReviewService {
    Review createReview(Long hotelId, Long userId, int rating, String comment);
    Review updateReview(Long reviewId, Long userId, int rating, String comment);
    void deleteReview(Long reviewId, Long userId);
    Review getReviewById(Long reviewId);
    List<Review> getReviewsByHotel(Long hotelId);
    List<Review> getReviewsByUser(Long userId);
    boolean canUserReviewHotel(Long hotelId, Long userId);
    double getAverageRatingForHotel(Long hotelId);
    long getReviewCountForHotel(Long hotelId);
} 