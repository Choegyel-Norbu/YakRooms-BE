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

	List<BookingResponse> getUserBookings(Long userId);

	List<BookingResponse> getBookingsByHotel(Long hotelId);
	
	public Page<BookingResponse> listAllBooking(Pageable pageable);
	
	public List<BookingResponse> listAllBookingNoPaginaiton();

	BookingResponse confirmBooking(Long bookingId);

	boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut);

	BookingResponse getBookingDetails(Long bookingId);
	
    public boolean updateBookingStatus(Long bookingId, String newStatus);
    
    public void deleteBookingById(Long bookingId);

    Page<BookingResponse> listAllBookingByHotel(Long hotelId, Pageable pageable);

    List<BookingStatisticsDTO> getBookingStatisticsByMonth(String startDate);
    
    List<BookingStatisticsDTO> getBookingStatisticsByMonthAndHotel(String startDate, Long hotelId);
    
    List<MonthlyRevenueStatsDTO> getMonthlyRevenueStats(Long hotelId, String startDate);
    
    /**
     * Verify booking by passcode.
     * 
     * @param passcode The passcode to verify
     * @return PasscodeVerificationDTO with verification result and booking details
     */
    PasscodeVerificationDTO verifyBookingByPasscode(String passcode);

    /**
     * Get all bookings for a specific user.
     * 
     * @param userId The user ID
     * @return List of booking responses
     */
    List<BookingResponse> getAllBookingsByUserId(Long userId);

    /**
     * Get all bookings for a specific user with pagination.
     * 
     * @param userId The user ID
     * @param pageable Pagination parameters
     * @return Page of booking responses
     */
    Page<BookingResponse> getAllBookingsByUserId(Long userId, Pageable pageable);

    /**
     * Get all bookings for a specific user by status.
     * 
     * @param userId The user ID
     * @param status The booking status to filter by
     * @return List of booking responses
     */
    List<BookingResponse> getAllBookingsByUserIdAndStatus(Long userId, String status);
    
    /**
     * Change booking status to CHECKED_IN for stats tracking.
     * 
     * @param bookingId The booking ID to update
     * @return true if status was successfully changed, false otherwise
     */
    boolean changeBookingToCheckIn(Long bookingId);
}
