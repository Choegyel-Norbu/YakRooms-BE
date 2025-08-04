package com.yakrooms.be.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yakrooms.be.dto.BookingStatisticsDTO;
import com.yakrooms.be.dto.MonthlyRevenueStatsDTO;
import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.enums.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Optimized user bookings with entity graph
    @EntityGraph("Booking.withDetails")
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Booking> findAllBookingsByUserIdWithDetails(@Param("userId") Long userId);

    @EntityGraph("Booking.withDetails")
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    Page<Booking> findAllBookingsByUserIdWithDetails(@Param("userId") Long userId, Pageable pageable);

    // Hotel bookings with optimized entity graph
    @EntityGraph("Booking.minimal")
    @Query("SELECT b FROM Booking b WHERE b.hotel.id = :hotelId ORDER BY b.createdAt DESC")
    List<Booking> findAllByHotelIdOptimized(@Param("hotelId") Long hotelId);

    @EntityGraph("Booking.minimal")
    @Query("SELECT b FROM Booking b WHERE b.hotel.id = :hotelId ORDER BY b.createdAt DESC")
    Page<Booking> findAllByHotelIdOptimized(@Param("hotelId") Long hotelId, Pageable pageable);

    // Status-based queries
    @EntityGraph("Booking.withDetails")
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status = :status ORDER BY b.createdAt DESC")
    List<Booking> findAllBookingsByUserIdAndStatus(@Param("userId") Long userId, @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.hotel.id = :hotelId AND b.user.id = :userId AND b.status = :status")
    List<Booking> findByHotelIdAndUserIdAndStatus(@Param("hotelId") Long hotelId, 
                                                   @Param("userId") Long userId, 
                                                   @Param("status") BookingStatus status);

    // Room availability check - optimized with index
    @Query("""
        SELECT b FROM Booking b 
        WHERE b.room.id = :roomId 
        AND b.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')
        AND b.checkInDate < :checkOut 
        AND b.checkOutDate > :checkIn
        """)
    List<Booking> findConflictingBookings(@Param("roomId") Long roomId, 
                                         @Param("checkIn") LocalDate checkIn,
                                         @Param("checkOut") LocalDate checkOut);

    // Passcode operations
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.passcode = :passcode")
    boolean existsByPasscode(@Param("passcode") String passcode);

    @EntityGraph("Booking.withDetails")
    Optional<Booking> findByPasscode(String passcode);

    // Batch operations for performance
    @Modifying
    @Query("UPDATE Booking b SET b.status = :status WHERE b.id IN :bookingIds")
    void updateStatusForBookings(@Param("bookingIds") List<Long> bookingIds, @Param("status") BookingStatus status);
    
    // Batch delete operations
    @Modifying
    @Query("DELETE FROM Booking b WHERE b.hotel.id = :hotelId")
    void deleteByHotelIdInBatch(@Param("hotelId") Long hotelId);

    // Dashboard and analytics queries
    @Query(value = """
        SELECT 
            DATE_FORMAT(b.created_at, '%Y-%m') as monthYear,
            COUNT(b.id) as bookingCount
        FROM booking b 
        WHERE b.created_at >= :startDate
        GROUP BY DATE_FORMAT(b.created_at, '%Y-%m')
        ORDER BY DATE_FORMAT(b.created_at, '%Y-%m')
        """, nativeQuery = true)
    List<BookingStatisticsDTO> getBookingStatisticsByMonth(@Param("startDate") LocalDateTime startDate);

    @Query(value = """
        WITH RECURSIVE month_series AS (
            SELECT 
                DATE(:startDate) as month_start,
                DATE_FORMAT(:startDate, '%Y-%m') as month_year
            UNION ALL
            SELECT 
                DATE_ADD(month_start, INTERVAL 1 MONTH),
                DATE_FORMAT(DATE_ADD(month_start, INTERVAL 1 MONTH), '%Y-%m')
            FROM month_series
            WHERE month_start < LAST_DAY(CURRENT_DATE - INTERVAL 1 MONTH) + INTERVAL 1 DAY
        )
        SELECT 
            ms.month_year as monthYear,
            COALESCE(COUNT(b.id), 0) as bookingCount
        FROM month_series ms
        LEFT JOIN booking b ON DATE_FORMAT(b.created_at, '%Y-%m') = ms.month_year
            AND b.hotel_id = :hotelId
            AND b.status != 'CANCELLED'
        GROUP BY ms.month_year
        ORDER BY ms.month_year
        """, nativeQuery = true)
    List<BookingStatisticsDTO> getBookingStatisticsByMonthAndHotel(@Param("startDate") LocalDate startDate, 
                                                                   @Param("hotelId") Long hotelId);

    @Query(value = """
        WITH RECURSIVE month_series AS (
            SELECT 
                DATE(:startDate) as month_start,
                DATE_FORMAT(:startDate, '%Y-%m') as month_year
            UNION ALL
            SELECT 
                DATE_ADD(month_start, INTERVAL 1 MONTH),
                DATE_FORMAT(DATE_ADD(month_start, INTERVAL 1 MONTH), '%Y-%m')
            FROM month_series
            WHERE month_start < LAST_DAY(CURRENT_DATE - INTERVAL 1 MONTH) + INTERVAL 1 DAY
        )
        SELECT 
            ms.month_year as monthYear,
            COALESCE(SUM(b.total_price), 0) as totalRevenue,
            COUNT(b.id) as bookingCount,
            COALESCE(AVG(b.total_price), 0) as averageBookingValue
        FROM month_series ms
        LEFT JOIN booking b ON DATE_FORMAT(b.created_at, '%Y-%m') = ms.month_year
            AND b.status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT')
            AND b.hotel_id = :hotelId
        GROUP BY ms.month_year
        ORDER BY ms.month_year
        """, nativeQuery = true)
    List<MonthlyRevenueStatsDTO> getMonthlyRevenueStats(@Param("hotelId") Long hotelId,
                                                        @Param("startDate") LocalDate startDate);

    // Performance monitoring queries
    @Query("""
        SELECT b FROM Booking b 
        WHERE b.checkInDate = :date 
        AND b.status = 'CONFIRMED'
        ORDER BY b.room.roomNumber
        """)
    List<Booking> findTodayCheckIns(@Param("date") LocalDate date);

    @Query("""
        SELECT b FROM Booking b 
        WHERE b.checkOutDate = :date 
        AND b.status = 'CHECKED_IN'
        ORDER BY b.room.roomNumber
        """)
    List<Booking> findTodayCheckOuts(@Param("date") LocalDate date);

    // Recent bookings for dashboard
    @EntityGraph("Booking.minimal")
    @Query("""
        SELECT b FROM Booking b 
        WHERE b.hotel.id = :hotelId 
        AND b.createdAt >= :since
        ORDER BY b.createdAt DESC
        """)
    List<Booking> findRecentBookings(@Param("hotelId") Long hotelId, @Param("since") LocalDateTime since);

    // Optimized count queries
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.hotel.id = :hotelId AND b.status = :status")
    long countBookingsByHotelAndStatus(@Param("hotelId") Long hotelId, @Param("status") BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId AND b.status = :status")
    long countBookingsByUserAndStatus(@Param("userId") Long userId, @Param("status") BookingStatus status);

    // Revenue calculations
    @Query("""
        SELECT COALESCE(SUM(b.totalPrice), 0) 
        FROM Booking b 
        WHERE b.hotel.id = :hotelId 
        AND b.status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT')
        AND b.createdAt BETWEEN :startDate AND :endDate
        """)
    java.math.BigDecimal getTotalRevenueByPeriod(@Param("hotelId") Long hotelId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
}