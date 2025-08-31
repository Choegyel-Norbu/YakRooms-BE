package com.yakrooms.be.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.projection.RoomStatusProjection;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Basic find methods - kept for simple use cases
    List<Room> findByHotelId(Long hotelId);

    // Batch delete operations
    @Modifying
    @Query("DELETE FROM Room r WHERE r.hotel.id = :hotelId")
    void deleteByHotelIdInBatch(@Param("hotelId") Long hotelId);

    // Optimized single room fetch (basic data only)
    @Query("SELECT r FROM Room r " +
           "WHERE r.id = :roomId")
    Optional<Room> findByIdWithItems(@Param("roomId") Long roomId);

    // Optimized batch fetching for multiple rooms (basic data only)
    @Query("SELECT r FROM Room r " +
           "WHERE r.hotel.id = :hotelId " +
           "ORDER BY r.roomNumber")
    List<Room> findByHotelIdWithItems(@Param("hotelId") Long hotelId);

    // Batch collection fetching methods - most efficient approach
    @Query(value = "SELECT ra.room_id, ra.amenity FROM room_amenities ra JOIN room r ON ra.room_id = r.id WHERE r.hotel_id = :hotelId", nativeQuery = true)
    List<Object[]> findAmenitiesByHotelIdWithRoomId(@Param("hotelId") Long hotelId);

    @Query(value = "SELECT ri.room_id, ri.url FROM room_image_urls ri JOIN room r ON ri.room_id = r.id WHERE r.hotel_id = :hotelId", nativeQuery = true)
    List<Object[]> findImageUrlsByHotelIdWithRoomId(@Param("hotelId") Long hotelId);

    // Single room collection fetching - for individual room operations
    @Query(value = "SELECT amenity FROM room_amenities WHERE room_id = :roomId", nativeQuery = true)
    List<String> findAmenitiesByRoomId(@Param("roomId") Long roomId);

    @Query(value = "SELECT url FROM room_image_urls WHERE room_id = :roomId", nativeQuery = true)
    List<String> findImageUrlsByRoomId(@Param("roomId") Long roomId);

    // Available rooms with pagination
    @Query(value = "SELECT * FROM room WHERE hotel_id = :hotelId AND is_available = true", 
           countQuery = "SELECT count(*) FROM room WHERE hotel_id = :hotelId AND is_available = true", 
           nativeQuery = true)
    Page<Room> findActiveAvailableRoomsByHotelId(@Param("hotelId") Long hotelId, Pageable pageable);
    
 // Option 1: Replace with JPQL query that fetches collections
    @Query("SELECT r FROM Room r " +
           "LEFT JOIN FETCH r.amenities " +
           "LEFT JOIN FETCH r.imageUrl " +
           "WHERE r.hotel.id = :hotelId AND r.isAvailable = true " +
           "ORDER BY r.roomNumber")
    Page<Room> findActiveAvailableRoomsByHotelIdWithCollections(@Param("hotelId") Long hotelId, Pageable pageable);

    // Option 2: Create a projection-based method (more efficient)
    @Query(value = """
            SELECT 
                r.id,
                r.hotel_id,
                r.room_number,
                r.room_type,
                r.is_available,
                r.price,
                r.max_guests,
                r.description,
                r.created_at,
                r.updated_at
            FROM room r 
            WHERE r.hotel_id = :hotelId 
            ORDER BY r.room_number
            """, 
           countQuery = "SELECT count(*) FROM room WHERE hotel_id = :hotelId AND is_available = true",
           nativeQuery = true)
    Page<Room> findActiveAvailableRoomsBasicData(@Param("hotelId") Long hotelId, Pageable pageable);

   
    // Helper method: Batch fetch collections for a page of rooms
    @Query(value = "SELECT ra.room_id, ra.amenity FROM room_amenities ra WHERE ra.room_id IN :roomIds", nativeQuery = true)
    List<Object[]> findAmenitiesByRoomIds(@Param("roomIds") List<Long> roomIds);

    @Query(value = "SELECT ri.room_id, ri.url FROM room_image_urls ri WHERE ri.room_id IN :roomIds", nativeQuery = true)
    List<Object[]> findImageUrlsByRoomIds(@Param("roomIds") List<Long> roomIds);
    
    // Bulk update room availability for scheduler operations
    @Modifying
    @Query("UPDATE Room r SET r.isAvailable = :available WHERE r.id IN :roomIds")
    int bulkUpdateRoomAvailability(@Param("roomIds") List<Long> roomIds, @Param("available") boolean available);

    // Room status projections - optimized for dashboard/status views
    @Query(value = """
            SELECT
                r.room_number AS roomNumber,
                r.room_type AS roomType,
                CASE
                    WHEN r.is_available = 0 AND b.status = 'CHECKED_IN' THEN 'Occupied'
                    WHEN r.is_available = 0 AND b.status = 'CONFIRMED' THEN 'Confirmed'
                    WHEN r.is_available = 1 THEN 'Available'
                    WHEN r.is_available = 0 AND b.status IS NULL THEN 'Under Repair'
                    ELSE 'Occupied'
                END AS roomStatus,
                CASE
                    WHEN b.status = 'CHECKED_IN' THEN u.name
                    WHEN b.status = 'CONFIRMED' THEN CONCAT(u.name, ' (Not Arrived)')
                    ELSE 'No guest'
                END AS guestName,
                b.check_out_date AS checkOutDate
            FROM room r
            LEFT JOIN (
                SELECT b1.room_id, b1.status, b1.user_id, b1.check_out_date
                FROM booking b1
                INNER JOIN (
                    SELECT room_id, status, user_id, check_out_date
                    FROM (
                        SELECT room_id, status, user_id, check_out_date,
                               ROW_NUMBER() OVER (PARTITION BY room_id ORDER BY 
                                   CASE 
                                       WHEN status = 'CHECKED_IN' THEN check_in_date
                                       WHEN status = 'CONFIRMED' THEN check_in_date
                                       ELSE created_at
                                   END DESC) as rn
                        FROM booking 
                        WHERE status IN ('CHECKED_IN', 'CONFIRMED')
                    ) ranked_bookings
                    WHERE rn = 1
                ) b2 ON b1.room_id = b2.room_id 
                    AND b1.status = b2.status 
                    AND b1.user_id = b2.user_id 
                    AND b1.check_out_date = b2.check_out_date
            ) b ON r.id = b.room_id
            LEFT JOIN users u ON b.user_id = u.id
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
                    WHEN r.is_available = 0 AND b.status = 'CONFIRMED' THEN 'Confirmed'
                    WHEN r.is_available = 1 THEN 'Available'
                    WHEN r.is_available = 0 AND b.status IS NULL THEN 'Under Repair'
                    ELSE 'Occupied'
                END AS roomStatus,
                CASE
                    WHEN b.status = 'CHECKED_IN' THEN u.name
                    WHEN b.status = 'CONFIRMED' THEN CONCAT(u.name, ' (Not Arrived)')
                    ELSE 'No guest'
                END AS guestName,
                b.check_out_date AS checkOutDate
            FROM room r
            LEFT JOIN (
                SELECT b1.room_id, b1.status, b1.user_id, b1.check_out_date
                FROM booking b1
                INNER JOIN (
                    SELECT room_id, status, user_id, check_out_date
                    FROM (
                        SELECT room_id, status, user_id, check_out_date,
                               ROW_NUMBER() OVER (PARTITION BY room_id ORDER BY 
                                   CASE 
                                       WHEN status = 'CHECKED_IN' THEN check_in_date
                                       WHEN status = 'CONFIRMED' THEN check_in_date
                                       ELSE created_at
                                   END DESC) as rn
                        FROM booking 
                        WHERE status IN ('CHECKED_IN', 'CONFIRMED')
                    ) ranked_bookings
                    WHERE rn = 1
                ) b2 ON b1.room_id = b2.room_id 
                    AND b1.status = b2.status 
                    AND b1.user_id = b2.user_id 
                    AND b1.check_out_date = b2.check_out_date
            ) b ON r.id = b.room_id
            LEFT JOIN users u ON b.user_id = u.id
            WHERE r.hotel_id = :hotelId 
            AND r.room_number LIKE CONCAT('%', :roomNumber, '%')
            ORDER BY r.room_number
            """, nativeQuery = true)
    Page<RoomStatusProjection> getRoomStatusByHotelIdAndRoomNumber(
            @Param("hotelId") Long hotelId, 
            @Param("roomNumber") String roomNumber, 
            Pageable pageable);
}
