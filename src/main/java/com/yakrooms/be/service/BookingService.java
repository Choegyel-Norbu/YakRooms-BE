package com.yakrooms.be.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
}
