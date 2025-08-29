package com.yakrooms.be.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yakrooms.be.dto.BookingStatisticsDTO;
import com.yakrooms.be.dto.MonthlyRevenueStatsDTO;
import com.yakrooms.be.dto.PasscodeVerificationDTO;
import com.yakrooms.be.dto.request.BookingRequest;
import com.yakrooms.be.dto.response.BookingResponse;

@Service
public interface BookingService {
	BookingResponse createBooking(BookingRequest request);

	void cancelBooking(Long bookingId, Long userId);

	List<BookingResponse> getBookingsByHotel(Long hotelId);
	
	public Page<BookingResponse> listAllBooking(Pageable pageable);

	BookingResponse confirmBooking(Long bookingId);

	boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut);

	/**
	 * Check if a room is available for advance booking (future dates).
	 * This method specifically checks for CONFIRMED bookings that might conflict
	 * with the requested date range.
	 * 
	 * @param request The booking request containing roomId, checkInDate, checkOutDate, and other details
	 * @return true if the room is available for advance booking, false otherwise
	 */
	boolean isRoomAvailableForAdvanceBooking(BookingRequest request);

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

}
