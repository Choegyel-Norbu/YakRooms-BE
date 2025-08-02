package com.yakrooms.be.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yakrooms.be.dto.RoomResponseDTO;
import com.yakrooms.be.dto.RoomStatusDTO;
import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.projection.RoomStatusProjection;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

	List<Room> findByHotelId(Long hotelId);

	@Query("SELECT DISTINCT r FROM Room r " +
		   "LEFT JOIN FETCH r.amenities " +
		   "LEFT JOIN FETCH r.imageUrl " +
		   "LEFT JOIN FETCH r.items " +
		   "WHERE r.hotel.id = :hotelId")
	List<Room> findByHotelIdWithCollections(@Param("hotelId") Long hotelId);

	@Query("SELECT DISTINCT r FROM Room r " +
		   "LEFT JOIN FETCH r.amenities " +
		   "LEFT JOIN FETCH r.imageUrl " +
		   "LEFT JOIN FETCH r.items " +
		   "WHERE r.id = :roomId")
	java.util.Optional<Room> findByIdWithCollections(@Param("roomId") Long roomId);

	@Query(value = "SELECT * FROM room WHERE hotel_id = :hotelId AND is_available = true", countQuery = "SELECT count(*) FROM room WHERE hotel_id = :hotelId AND is_available = true", nativeQuery = true)
	Page<Room> findActiveAvailableRoomsByHotelId(@Param("hotelId") Long hotelId, Pageable pageable);

	@Query(value = "SELECT " + "    r.id," + "    r.room_type," + "    r.description," + "    r.price,"
			+ "    r.is_available," + "    r.max_guests," + "    h.name AS hotel_name,"
			+ "    GROUP_CONCAT(ra.amenity) AS amenities " + "FROM room r "
			+ "JOIN room_amenities ra ON r.id = ra.room_id " + "JOIN hotels h ON h.id = r.hotel_id "
			+ "GROUP BY r.id WHERE h.id = :hotelId", nativeQuery = true)
	List<Object[]> getRoomsForHotel(@Param("hotelId") Long hotelId);

	@Query(value = """
			SELECT
			    r.room_number AS roomNumber,
			    r.room_type AS roomType,
			    CASE
			        WHEN r.is_available = 0 AND b.status = 'CHECKED_IN' THEN 'Booked'
			        WHEN r.is_available = 1 THEN 'Available'
			        WHEN r.is_available = 0 AND b.status IS NULL THEN 'Under Repair'
			        ELSE 'Occupied'
			    END AS roomStatus,
			    CASE
			        WHEN b.status = 'CHECKED_IN' THEN u.name
			        ELSE 'No guest'
			    END AS guestName,
			    b.check_out_date AS checkOutDate
			FROM room r
			LEFT JOIN booking b
			    ON r.id = b.room_id AND b.status = 'CHECKED_IN'
			LEFT JOIN users u
			    ON b.user_id = u.id
			WHERE r.hotel_id = :hotelId
			ORDER BY r.room_number
			""", nativeQuery = true)
	Page<RoomStatusProjection> getRoomStatusByHotelId(@Param("hotelId") Long hotelId, Pageable pageable);

	@Query(value = """
			SELECT
			    r.room_number AS roomNumber,
			    r.room_type AS roomType,
			    CASE
			        WHEN r.is_available = 0 AND b.status = 'CHECKED_IN' THEN 'Booked'
			        WHEN r.is_available = 1 THEN 'Available'
			        WHEN r.is_available = 0 AND b.status IS NULL THEN 'Under Repair'
			        ELSE 'Occupied'
			    END AS roomStatus,
			    CASE
			        WHEN b.status = 'CHECKED_IN' THEN u.name
			        ELSE 'No guest'
			    END AS guestName,
			    b.check_out_date AS checkOutDate
			FROM room r
			LEFT JOIN booking b
			    ON r.id = b.room_id AND b.status = 'CHECKED_IN'
			LEFT JOIN users u
			    ON b.user_id = u.id
			WHERE r.hotel_id = :hotelId 
			AND r.room_number LIKE CONCAT('%', :roomNumber, '%')
			ORDER BY r.room_number
			""", nativeQuery = true)
	Page<RoomStatusProjection> getRoomStatusByHotelIdAndRoomNumber(@Param("hotelId") Long hotelId, @Param("roomNumber") String roomNumber, Pageable pageable);

}