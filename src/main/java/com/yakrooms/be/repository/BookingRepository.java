package com.yakrooms.be.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yakrooms.be.dto.BookingStatisticsDTO;
import com.yakrooms.be.dto.MonthlyRevenueStatsDTO;
import com.yakrooms.be.model.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

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
	List<Booking> findBookingsForRoom(@Param("roomId") Long roomId, @Param("checkIn") LocalDate checkIn,
			@Param("checkOut") LocalDate checkOut);

	@Query(value = """
			SELECT
			    DATE_FORMAT(b.created_at, '%Y-%m') AS monthYear,
			    COUNT(b.id) AS bookingCount
			FROM
			    booking b
			WHERE
			    b.created_at >= :startDate
			GROUP BY
			    DATE_FORMAT(b.created_at, '%Y-%m')
			ORDER BY
			    DATE_FORMAT(b.created_at, '%Y-%m')
			""", nativeQuery = true)
	List<BookingStatisticsDTO> getBookingStatisticsByMonth(@Param("startDate") String startDate);

	@Query(value = """
			WITH RECURSIVE month_series AS (
			    SELECT
			        DATE(:startDate) AS month_start,
			        DATE_FORMAT(:startDate, '%Y-%m') AS month_year
			    UNION ALL
			    SELECT
			        DATE_ADD(month_start, INTERVAL 1 MONTH),
			        DATE_FORMAT(DATE_ADD(month_start, INTERVAL 1 MONTH), '%Y-%m')
			    FROM month_series
			    WHERE month_start < DATE_FORMAT(CURRENT_DATE, '%Y-%m-01')
			)
			SELECT
			    ms.month_year,
			    COALESCE(COUNT(b.id), 0) AS booking_count
			FROM
			    month_series ms
			LEFT JOIN
			    booking b
			    ON DATE_FORMAT(b.created_at, '%Y-%m') = ms.month_year
			    AND b.hotel_id = :hotelId
			GROUP BY
			    ms.month_year
			ORDER BY
			    ms.month_year
			""", nativeQuery = true)
	List<BookingStatisticsDTO> getBookingStatisticsByMonthAndHotel(
			@Param("startDate") String startDate, @Param("hotelId") Long hotelId);

	@Query(value = """
			WITH RECURSIVE month_series AS (
				SELECT
					DATE(:startDate) AS month_start,
					DATE_FORMAT(:startDate, '%Y-%m') AS month_year
				UNION ALL
				SELECT
					DATE_ADD(month_start, INTERVAL 1 MONTH),
					DATE_FORMAT(DATE_ADD(month_start, INTERVAL 1 MONTH), '%Y-%m')
				FROM month_series
				WHERE month_start < DATE_FORMAT(CURRENT_DATE, '%Y-%m-01')
			)
			SELECT
				ms.month_year,
				COALESCE(SUM(b.total_price), 0) AS total_revenue,
				COUNT(b.id) AS booking_count,
				COALESCE(AVG(b.total_price), 0) AS average_booking_value
			FROM
				month_series ms
			LEFT JOIN booking b ON DATE_FORMAT(b.created_at, '%Y-%m') = ms.month_year
				AND b.status != 'CANCELLED'
				AND b.hotel_id = :hotelId
			JOIN hotels h ON h.id = :hotelId
			GROUP BY ms.month_year, h.name
			ORDER BY ms.month_year
			""", nativeQuery = true)
	List<MonthlyRevenueStatsDTO> getMonthlyRevenueStats(
			@Param("hotelId") Long hotelId,
			@Param("startDate") String startDate // format: "yyyy-MM-dd"
	);

	
	boolean existsByPasscode(String passcode);

	
	Optional<Booking> findByPasscode(String passcode);


	@Query("SELECT b FROM Booking b " +
		   "LEFT JOIN FETCH b.hotel h " +
		   "LEFT JOIN FETCH b.room r " +
		   "WHERE b.user.id = :userId " +
		   "ORDER BY b.createdAt DESC")
	List<Booking> findAllBookingsByUserIdWithDetails(@Param("userId") Long userId);

	@Query("SELECT b FROM Booking b " +
		   "LEFT JOIN FETCH b.hotel h " +
		   "LEFT JOIN FETCH b.room r " +
		   "WHERE b.user.id = :userId " +
		   "ORDER BY b.createdAt DESC")
	Page<Booking> findAllBookingsByUserIdWithDetails(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT b FROM Booking b " +
		   "LEFT JOIN FETCH b.hotel h " +
		   "LEFT JOIN FETCH b.room r " +
		   "WHERE b.user.id = :userId AND b.status = :status " +
		   "ORDER BY b.createdAt DESC")
	List<Booking> findAllBookingsByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
}
