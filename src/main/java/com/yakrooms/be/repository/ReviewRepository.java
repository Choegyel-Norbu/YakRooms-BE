package com.yakrooms.be.repository;

import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.Review;
import com.yakrooms.be.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Batch delete operations
    @Modifying
    @Query("DELETE FROM Review r WHERE r.hotel.id = :hotelId")
    void deleteByHotelIdInBatch(@Param("hotelId") Long hotelId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.hotel.id = :hotelId")
    Optional<Double> findAverageRatingByHotel(@Param("hotelId") Long hotelId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.hotel.id = :hotelId")
    long countReviewsByHotel(@Param("hotelId") Long hotelId);

    @EntityGraph(value = "Review.withUser")
    List<Review> findAllByHotelIdOrderByCreatedAtDesc(Long hotelId);

    @EntityGraph(value = "Review.withUser")
    Page<Review> findByHotelIdOrderByCreatedAtDesc(Long hotelId, Pageable pageable);

    boolean existsByHotelAndUser(Hotel hotel, User user);
}