package com.yakrooms.be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yakrooms.be.model.entity.Restaurant;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long>{
	
	List<Restaurant> findByHotelId(Long hotelId);

	// Batch delete operations
	@Modifying
	@Query("DELETE FROM Restaurant r WHERE r.hotel.id = :hotelId")
	void deleteByHotelIdInBatch(@Param("hotelId") Long hotelId);


}
