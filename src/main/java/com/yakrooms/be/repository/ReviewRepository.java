package com.yakrooms.be.repository;

import com.yakrooms.be.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByHotelId(Long hotelId);
    List<Review> findByUserId(Long userId);
    long countByHotelId(Long hotelId);
} 