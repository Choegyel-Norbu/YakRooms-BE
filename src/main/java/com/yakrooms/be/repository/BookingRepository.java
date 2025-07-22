package com.yakrooms.be.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yakrooms.be.model.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>{
	
	@Query(value = "SELECT * FROM bookings WHERE user_id = :userId", nativeQuery = true)
	List<Booking> findAllByUserId(@Param("userId") Long userId);

	@Query(value = "SELECT * FROM bookings WHERE hotel_id = :hotelId", nativeQuery = true)
	List<Booking> findAllByHotelId(@Param("hotelId") Long hotelId);

	Page<Booking> findAllByHotelId(Long hotelId, Pageable pageable);

	List<Booking> findByHotelIdAndUserIdAndStatus(Long hotelId, Long userId, String status);

	@Query(value = """
	    SELECT * FROM bookings 
	    WHERE room_id = :roomId 
	    AND status != 'CANCELLED'
	    AND check_in_date < :checkOut 
	    AND check_out_date > :checkIn
	    """, nativeQuery = true)
	List<Booking> findBookingsForRoom(@Param("roomId") Long roomId, 
	                                  @Param("checkIn") LocalDate checkIn, 
	                                  @Param("checkOut") LocalDate checkOut);


}
