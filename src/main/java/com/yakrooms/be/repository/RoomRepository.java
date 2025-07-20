package com.yakrooms.be.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yakrooms.be.dto.RoomResponseDTO;
import com.yakrooms.be.model.entity.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

	List<Room> findByHotelId(Long hotelId);

	@Query(value = "SELECT * FROM room WHERE hotel_id = :hotelId AND is_available = true", countQuery = "SELECT count(*) FROM room WHERE hotel_id = :hotelId AND is_available = true", nativeQuery = true)
	Page<Room> findActiveAvailableRoomsByHotelId(@Param("hotelId") Long hotelId, Pageable pageable);

	@Query(value = "SELECT \n" + "    r.id,\n" + "    r.room_type,\n" + "    r.description,\n" + "    r.price,\n"
			+ "    r.is_available,\n" + "    r.max_guests,\n" + "    h.name AS hotel_name,\n"
			+ "    GROUP_CONCAT(ra.amenity) AS amenities\n" + "FROM room r\n"
			+ "JOIN room_amenities ra ON r.id = ra.room_id\n" + "JOIN hotels h ON h.id = r.hotel_id\n"
			+ "GROUP BY r.id WHERE h.id = :hotelId;\n" + "", nativeQuery = true)
	List<Object[]> getRoomsForHotel(@Param("hotelId") Long hotelId);

}
