package com.yakrooms.be.service;

import com.yakrooms.be.dto.request.ReviewRequest;
import com.yakrooms.be.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {
    double getAverageRatingForHotel(Long hotelId);
    long getReviewCountForHotel(Long hotelId);
    public ReviewResponse createReview(ReviewRequest reviewRequest);
    List<ReviewResponse> getAllReviewsForHotel(Long hotelId);
    Page<ReviewResponse> getReviewsForHotelPaginated(Long hotelId, Pageable pageable);
} 