package com.yakrooms.be.repository;

import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.Review;
import com.yakrooms.be.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

	/**
	 * Get average rating for a specific hotel
	 */
	@Query("SELECT AVG(r.rating) FROM Review r WHERE r.hotel.id = :hotelId")
	Optional<Double> findAverageRatingByHotel(@Param("hotelId") Long hotelId);

	/**
	 * Count reviews for a hotel
	 */
	@Query("SELECT COUNT(r) FROM Review r WHERE r.hotel.id = :hotelId")
	long countReviewsByHotel(@Param("hotelId") Long hotelId);

	/**
	 * Check if user has already reviewed a hotel
	 */
	@Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r WHERE r.hotel = :hotel AND r.user = :user")
	boolean existsByHotelAndUser(@Param("hotel") Hotel hotel, @Param("user") User user);

	/**
	 * Get all reviews for a specific hotel with user data, ordered by creation date descending
	 */
	@EntityGraph(value = "Review.withUser")
	List<Review> findAllByHotelIdOrderByCreatedAtDesc(Long hotelId);

	/**
	 * Get all reviews for a specific hotel with user and hotel data eagerly loaded
	 */
	@EntityGraph(value = "Review.withUserAndHotel")
	@Query("SELECT r FROM Review r WHERE r.hotel.id = :hotelId ORDER BY r.createdAt DESC")
	List<Review> findAllByHotelIdOrderByCreatedAtDescWithFull(@Param("hotelId") Long hotelId);

	/**
	 * Get paginated reviews for a specific hotel, ordered by creation date descending
	 * Uses EntityGraph for optimal loading
	 */
	@EntityGraph(value = "Review.withUser")
	Page<Review> findByHotelIdOrderByCreatedAtDesc(Long hotelId, Pageable pageable);

	/**
	 * Alternative optimized query for paginated reviews with explicit JOIN FETCH
	 */
	@Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.hotel.id = :hotelId ORDER BY r.createdAt DESC")
	Page<Review> findAllByHotelIdWithUser(@Param("hotelId") Long hotelId, Pageable pageable);



}