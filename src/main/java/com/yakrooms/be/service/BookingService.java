package com.yakrooms.be.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yakrooms.be.dto.BookingStatisticsDTO;
import com.yakrooms.be.dto.MonthlyRevenueStatsDTO;
import com.yakrooms.be.dto.PasscodeVerificationDTO;
import com.yakrooms.be.dto.response.BookingResponse;

public interface BookingService {
	// createBooking method removed - now handled by UnifiedBookingService

	void cancelBooking(Long bookingId, Long userId);

	List<BookingResponse> getBookingsByHotel(Long hotelId);
	
	public Page<BookingResponse> listAllBooking(Pageable pageable);

	// confirmBooking method removed - functionality now handled by updateBookingStatus

	boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut);

	BookingResponse getBookingDetails(Long bookingId);
	
    public boolean updateBookingStatus(Long bookingId, String newStatus);
    
    public void deleteBookingById(Long bookingId);

    Page<BookingResponse> listAllBookingByHotel(Long hotelId, Pageable pageable);

    List<BookingStatisticsDTO> getBookingStatisticsByMonth(String startDate);
    
    List<BookingStatisticsDTO> getBookingStatisticsByMonthAndHotel(String startDate, Long hotelId);
    
    List<MonthlyRevenueStatsDTO> getMonthlyRevenueStats(Long hotelId, String startDate);
    
    PasscodeVerificationDTO verifyBookingByPasscode(String passcode);

    List<BookingResponse> getAllBookingsByUserId(Long userId);

    Page<BookingResponse> getAllBookingsByUserId(Long userId, Pageable pageable);

    List<BookingResponse> getAllBookingsByUserIdAndStatus(Long userId, String status);

    // ========== SEARCH METHODS ==========
    
    // Individual search methods for optimal performance
    Page<BookingResponse> searchBookingsByCid(String cid, Long hotelId, Pageable pageable);
    
    Page<BookingResponse> searchBookingsByPhone(String phone, Long hotelId, Pageable pageable);
    
    Page<BookingResponse> searchBookingsByCheckInDate(LocalDate checkInDate, Long hotelId, Pageable pageable);
    
    Page<BookingResponse> searchBookingsByCheckOutDate(LocalDate checkOutDate, Long hotelId, Pageable pageable);
    
    Page<BookingResponse> searchBookingsByStatus(String status, Long hotelId, Pageable pageable);
    
    Page<BookingResponse> searchBookingsByDateRange(LocalDate startDate, LocalDate endDate, Long hotelId, Pageable pageable);
    


}
