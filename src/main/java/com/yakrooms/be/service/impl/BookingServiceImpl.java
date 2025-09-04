package com.yakrooms.be.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yakrooms.be.dto.BookingStatisticsDTO;
import com.yakrooms.be.dto.MonthlyRevenueStatsDTO;
import com.yakrooms.be.dto.PasscodeVerificationDTO;
import com.yakrooms.be.dto.BookingChangeEvent;
import com.yakrooms.be.dto.mapper.BookingMapper;
import com.yakrooms.be.dto.response.BookingResponse;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.exception.BusinessException;
import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.BookingStatus;
import com.yakrooms.be.repository.BookingRepository;
import com.yakrooms.be.repository.RoomRepository;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.service.BookingService;
import com.yakrooms.be.service.BookingWebSocketService;
import com.yakrooms.be.service.BookingValidationService;
import com.yakrooms.be.service.NotificationService;
import com.yakrooms.be.service.RoomAvailabilityService;
import com.yakrooms.be.util.PasscodeGenerator;



/**
 * Refactored BookingServiceImpl that uses the new unified services.
 * This implementation delegates to specialized services for room availability,
 * payment management, and validation, ensuring consistency and atomicity.
 * 
 * @author YakRooms Team
 * @version 2.0
 */
@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);
    private static final int MAX_PASSCODE_ATTEMPTS = 5;
    
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final BookingWebSocketService bookingWebSocketService;
    private final BookingValidationService bookingValidationService;
    private final RoomAvailabilityService roomAvailabilityService;
    private final NotificationService notificationService;

    public BookingServiceImpl(BookingRepository bookingRepository,
            RoomRepository roomRepository,
            UserRepository userRepository,
            BookingMapper bookingMapper,
            BookingWebSocketService bookingWebSocketService,
            BookingValidationService bookingValidationService,
            RoomAvailabilityService roomAvailabilityService,
            NotificationService notificationService) {
        
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.bookingMapper = bookingMapper;
        this.bookingWebSocketService = bookingWebSocketService;
        this.bookingValidationService = bookingValidationService;
        this.roomAvailabilityService = roomAvailabilityService;
        this.notificationService = notificationService;
    }

    @Override
    public void deleteBookingById(Long bookingId) {
        logger.info("Deleting booking: {}", bookingId);

        Booking booking = fetchBookingById(bookingId);

        // Do not flip global availability on delete per new policy

        bookingRepository.delete(booking);
        logger.info("Successfully deleted booking: {}", bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingStatisticsDTO> getBookingStatisticsByMonth(String startDate) {
        validateStartDate(startDate);
        LocalDateTime startDateTime = LocalDate.parse(startDate).atStartOfDay();
        return bookingRepository.getBookingStatisticsByMonth(startDateTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingStatisticsDTO> getBookingStatisticsByMonthAndHotel(String startDate, Long hotelId) {
        validateStartDate(startDate);
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
        LocalDate startLocalDate = LocalDate.parse(startDate);
        return bookingRepository.getBookingStatisticsByMonthAndHotel(startLocalDate, hotelId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyRevenueStatsDTO> getMonthlyRevenueStats(Long hotelId, String startDate) {
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
        validateStartDate(startDate);
        LocalDate startLocalDate = LocalDate.parse(startDate);
        return bookingRepository.getMonthlyRevenueStats(hotelId, startLocalDate);
    }

    @Override
    @Transactional(readOnly = true)
    public PasscodeVerificationDTO verifyBookingByPasscode(String passcode) {
        if (passcode == null || passcode.trim().isEmpty()) {
            return new PasscodeVerificationDTO(false, "Passcode cannot be empty");
        }

        // Validate passcode format
        if (!PasscodeGenerator.isValidPasscode(passcode.trim())) {
            logger.warn("Invalid passcode format: {}", passcode);
            return new PasscodeVerificationDTO(false, "Invalid passcode format.");
        }

        Optional<Booking> bookingOpt = bookingRepository.findByPasscode(passcode.trim());

        if (bookingOpt.isEmpty()) {
            logger.warn("Invalid passcode verification attempt: {}", passcode);
            return new PasscodeVerificationDTO(false, "Invalid passcode. No booking found.");
        }

        Booking booking = bookingOpt.get();
        return validateAndCreateVerificationResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookingsByUserId(Long userId) {
        validateUserId(userId);
        return bookingRepository.findAllBookingsByUserIdWithDetails(userId)
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookingsByUserId(Long userId, Pageable pageable) {
        validateUserId(userId);
        return bookingRepository.findAllBookingsByUserIdWithDetails(userId, pageable)
                .map(bookingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookingsByUserIdAndStatus(Long userId, String status) {
        validateUserId(userId);
        BookingStatus bookingStatus = validateAndParseStatus(status);
        return bookingRepository.findAllBookingsByUserIdAndStatus(userId, bookingStatus)
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelBooking(Long bookingId, Long userId) {
        logger.info("Cancelling booking: {} by user: {}", bookingId, userId);

        Booking booking = fetchBookingById(bookingId);
        validateBookingOwnership(booking, userId);
        
        // Use the new validation service
        bookingValidationService.validateCancellation(booking);

        // Store check-in date before updating status
        LocalDate checkInDate = booking.getCheckInDate();
        
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Update room availability immediately if this was a same-day booking
        // This ensures cancelled same-day bookings don't leave rooms unavailable
        try {
            roomAvailabilityService.updateRoomAvailabilityForCancelledBooking(
                booking.getRoom().getId(), 
                checkInDate
            );
        } catch (Exception e) {
            logger.error("Failed to update room availability for cancelled booking {}: {}", bookingId, e.getMessage());
            // Don't fail the cancellation if room availability update fails
        }

        // Do not flip global availability on cancel per new policy

        logger.info("Successfully cancelled booking: {}", bookingId);
    }



    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByHotel(Long hotelId) {
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
        return bookingRepository.findAllByHotelIdOptimized(hotelId)
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    // confirmBooking method removed - functionality now handled by updateBookingStatus

    @Override
    @Transactional(readOnly = true)
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return isRoomAvailableForDates(roomId, checkIn, checkOut);
    }

    /**
     * Check if room is available for specific dates.
     * 
     * @param roomId The room ID
     * @param checkIn Check-in date
     * @param checkOut Check-out date
     * @return true if room is available
     */
    @Transactional(readOnly = true)
    public boolean isRoomAvailableForDates(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        List<Booking> conflictingBookings = getConflictingBookings(roomId, checkIn, checkOut);
        return conflictingBookings.isEmpty();
    }

    /**
     * Check if room is available for specific dates and times.
     * 
     * @param roomId The room ID
     * @param checkIn Check-in date
     * @param checkInTime Check-in time
     * @param checkOut Check-out date
     * @param checkOutTime Check-out time
     * @return true if room is available
     */
    @Transactional(readOnly = true)
    public boolean isRoomAvailableForDatesAndTimes(Long roomId, LocalDate checkIn, LocalTime checkInTime, 
                                                 LocalDate checkOut, LocalTime checkOutTime) {
        List<Booking> conflictingBookings = getConflictingBookingsWithTime(roomId, checkIn, checkInTime, checkOut, checkOutTime);
        return conflictingBookings.isEmpty();
    }

    /**
     * Get conflicting bookings for specific dates.
     * 
     * @param roomId The room ID
     * @param checkIn Check-in date
     * @param checkOut Check-out date
     * @return List of conflicting bookings
     */
    @Transactional(readOnly = true)
    public List<Booking> getConflictingBookings(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return bookingRepository.findConflictingBookings(roomId, checkIn, checkOut);
    }

    /**
     * Get conflicting bookings for specific dates and times.
     * 
     * @param roomId The room ID
     * @param checkIn Check-in date
     * @param checkInTime Check-in time
     * @param checkOut Check-out date
     * @param checkOutTime Check-out time
     * @return List of conflicting bookings
     */
    @Transactional(readOnly = true)
    public List<Booking> getConflictingBookingsWithTime(Long roomId, LocalDate checkIn, LocalTime checkInTime, 
                                                       LocalDate checkOut, LocalTime checkOutTime) {
        return bookingRepository.findConflictingBookings(roomId, checkIn, checkOut);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingDetails(Long bookingId) {
        validateBookingId(bookingId);
        Booking booking = fetchBookingById(bookingId);
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> listAllBooking(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(bookingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> listAllBookingByHotel(Long hotelId, Pageable pageable) {
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
        return bookingRepository.findAllByHotelIdOptimized(hotelId, pageable)
                .map(bookingMapper::toDto);
    }

    @Override
    @Transactional
    public boolean updateBookingStatus(Long bookingId, String newStatus) {
        logger.info("Updating booking status: {} to: {}", bookingId, newStatus);

        Booking booking = fetchBookingById(bookingId);
        BookingStatus status = validateAndParseStatus(newStatus);

        // Use the new validation service
        bookingValidationService.validateStatusTransition(booking.getStatus(), status);

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(status);
        bookingRepository.save(booking);

        // Handle room availability updates using the centralized service
        try {
            Room room = booking.getRoom();
            if (room != null) {
                switch (status) {
                    case CHECKED_IN:
                        // Guest checks in - room becomes unavailable
                        roomAvailabilityService.updateRoomAvailabilityForCheckIn(room.getId());
                        break;
                    case CHECKED_OUT:
                        // Guest checks out - room becomes available
                        roomAvailabilityService.updateRoomAvailabilityForCheckOut(room.getId());
                        break;
                    case CANCELLED:
                        // Booking cancelled - update availability based on check-in date
                        roomAvailabilityService.updateRoomAvailabilityForCancelledBooking(
                            room.getId(), 
                            booking.getCheckInDate()
                        );
                        break;
                    case CONFIRMED:
                        // Booking confirmed - update availability based on check-in date
                        roomAvailabilityService.updateRoomAvailabilityForConfirmedBooking(
                            room.getId(), 
                            booking.getCheckInDate()
                        );
                        break;
                    case CANCELLATION_REJECTED:
                        // Cancellation rejected - room remains unavailable as booking is still active
                        logger.debug("Cancellation rejected for booking {} - room availability unchanged", bookingId);
                        break;
                    default:
                        // Other statuses don't affect room availability
                        logger.debug("No room availability change needed for status: {} for booking {}", status, bookingId);
                        break;
                }
            } else {
                logger.warn("Room is null for booking {}, cannot update availability", bookingId);
            }
        } catch (Exception e) {
            logger.error("Failed to update room availability after status change to {} for booking {}: {}", 
                status, bookingId, e.getMessage());
            // Don't fail the entire operation if room update fails
        }

        // Broadcast WebSocket event for booking status change
        broadcastBookingStatusChange(booking, oldStatus, status);

        logger.info("Successfully updated booking status: {} to: {}", bookingId, status);
        return true;
    }

    @Override
    @Transactional
    public boolean rejectCancellationRequest(Long bookingId) {
        logger.info("Rejecting cancellation request for booking: {}", bookingId);

        // Fetch and validate the booking
        Booking booking = fetchBookingById(bookingId);
        
        // Validate that this is a cancellation request that can be rejected
        if (booking.getStatus() != BookingStatus.CANCELLATION_REQUESTED) {
            logger.warn("Cannot reject cancellation for booking {} with status: {}", 
                       bookingId, booking.getStatus());
            throw new BusinessException("Only bookings with CANCELLATION_REQUESTED status can be rejected");
        }

        // Store the old status for WebSocket broadcasting
        BookingStatus oldStatus = booking.getStatus();
        
        // Update status to CANCELLATION_REJECTED
        booking.setStatus(BookingStatus.CANCELLATION_REJECTED);
        bookingRepository.save(booking);

        // IMPORTANT: Do NOT call RoomAvailabilityService for rejection
        // The room should remain unavailable as the booking is still active
        logger.info("Booking {} status updated to CANCELLATION_REJECTED - room availability unchanged", bookingId);

        // Create notification for the guest about rejection
        try {
            notificationService.createCancellationRejectionNotification(booking);
            logger.info("Created cancellation rejection notification for booking: {}", bookingId);
        } catch (Exception e) {
            logger.error("Failed to create cancellation rejection notification for booking {}: {}", 
                        bookingId, e.getMessage());
            // Don't fail the entire operation if notification creation fails
        }

        // Broadcast WebSocket event for booking status change
        broadcastBookingStatusChange(booking, oldStatus, BookingStatus.CANCELLATION_REJECTED);

        logger.info("Successfully rejected cancellation request for booking: {}", bookingId);
        return true;
    }

    // ========== PRIVATE HELPER METHODS ==========

    // These validation methods are now handled by BookingValidationService
    // private void validateBookingRequest(BookingRequest request) - REMOVED
    // private Booking createBookingEntity(BookingRequest request, User guest, Hotel hotel, Room room) - REMOVED

    /**
     * Update room availability for a booking using the unified service.
     * This method is now deprecated in favor of the centralized RoomAvailabilityService.
     * 
     * @deprecated Use RoomAvailabilityService methods instead
     */
    @Deprecated
    private void updateRoomAvailabilityForBooking(Booking booking) {
        // This method is deprecated - use RoomAvailabilityService instead
        logger.warn("updateRoomAvailabilityForBooking is deprecated. Use RoomAvailabilityService methods instead.");
    }

    /**
     * Handle room availability changes for status transitions.
     * This method is now deprecated in favor of the centralized RoomAvailabilityService.
     * 
     * @deprecated Use RoomAvailabilityService methods instead
     */
    @Deprecated
    private void handleRoomAvailabilityForStatusChange(Booking booking, BookingStatus oldStatus,
            BookingStatus newStatus) {
        // This method is deprecated - use RoomAvailabilityService instead
        logger.warn("handleRoomAvailabilityForStatusChange is deprecated. Use RoomAvailabilityService methods instead.");
    }

    private void broadcastBookingStatusChange(Booking booking, BookingStatus oldStatus, BookingStatus newStatus) {
        try {
            // Create booking change event
            Long userId = booking.getUser() != null ? booking.getUser().getId() : null;
            BookingChangeEvent event = new BookingChangeEvent(
                    booking.getId(),
                    booking.getHotel().getId(),
                    userId,
                    oldStatus,
                    newStatus,
                    "BOOKING_STATUS_CHANGE",
                    String.format("Booking status changed from %s to %s", oldStatus, newStatus));

            // Broadcast the event via WebSocket
            bookingWebSocketService.broadcastBookingStatusChange(event);

            logger.info("Broadcasted booking status change event: {} -> {} for booking {}",
                    oldStatus, newStatus, booking.getId());
        } catch (Exception e) {
            logger.error("Failed to broadcast booking status change event for booking {}: {}",
                    booking.getId(), e.getMessage());
        }
    }

    private PasscodeVerificationDTO validateAndCreateVerificationResponse(Booking booking) {
        // Check if booking is cancelled
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return new PasscodeVerificationDTO(false, "This booking has been cancelled.");
        }

        // Check if booking is for past dates
        if (booking.isPastCheckIn()) {
            return new PasscodeVerificationDTO(false, "This booking is for a past date.");
        }

        // Create successful verification response
        PasscodeVerificationDTO verification = new PasscodeVerificationDTO(true, "Booking verified successfully!");
        verification.setBookingId(booking.getId());
        verification.setCheckInDate(booking.getCheckInDate());
        verification.setCheckOutDate(booking.getCheckOutDate());
        verification.setStatus(booking.getStatus());
        verification.setCreatedAt(booking.getCreatedAt());

        // Set related entity information
        if (booking.getUser() != null) {
            verification.setGuestName(booking.getUser().getName());
        }
        if (booking.getHotel() != null) {
            verification.setHotelName(booking.getHotel().getName());
        }
        if (booking.getRoom() != null) {
            verification.setRoomNumber(booking.getRoom().getRoomNumber());
        }

        return verification;
    }

    private String generateUniquePasscode() {
        logger.debug("Generating unique passcode with max attempts: {}", MAX_PASSCODE_ATTEMPTS);
        for (int attempt = 1; attempt <= MAX_PASSCODE_ATTEMPTS; attempt++) {
            try {
                String passcode = PasscodeGenerator.generatePasscode();

                if (passcode == null || passcode.trim().isEmpty()) {
                    logger.warn("Generated passcode is null or empty, retrying... Attempt: {}", attempt);
                    continue;
                }

                if (!bookingRepository.existsByPasscode(passcode)) {
                    logger.debug("Successfully generated unique passcode on attempt: {}", attempt);
                    return passcode;
                }

                if (attempt < MAX_PASSCODE_ATTEMPTS) {
                    logger.warn("Passcode collision detected, retrying... Attempt: {}", attempt);
                }
            } catch (Exception e) {
                logger.error("Error generating passcode on attempt: {}", attempt, e);
                if (attempt == MAX_PASSCODE_ATTEMPTS) {
                    throw new BusinessException("Failed to generate passcode due to system error", e);
                }
            }
        }

        logger.error("Unable to generate unique passcode after {} attempts", MAX_PASSCODE_ATTEMPTS);
        throw new BusinessException("Unable to generate unique passcode after " + MAX_PASSCODE_ATTEMPTS
                + " attempts. Please try again later.");
    }

    // Validation helper methods
    private User fetchUserById(Long userId) {
        return userRepository.findByIdWithCollections(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private Room fetchRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
    }

    private Booking fetchBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }

    private void validateBookingId(Long bookingId) {
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID cannot be null");
        }
    }

    private void validateStartDate(String startDate) {
        if (startDate == null || startDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Start date cannot be null or empty");
        }
        try {
            LocalDate.parse(startDate);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid start date format: " + startDate);
        }
    }

    private void validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out dates cannot be null");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
    }

    private BookingStatus validateAndParseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        try {
            return BookingStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid booking status: " + status);
        }
    }

    private void validateBookingOwnership(Booking booking, Long userId) {
        if (!booking.getUser().getId().equals(userId)) {
            throw new BusinessException("You can only modify your own bookings");
        }
    }

    private void validateBookingCancellation(Booking booking) {
        if (!booking.canBeCancelled()) {
            throw new BusinessException("Booking cannot be cancelled in current status: " + booking.getStatus());
        }
    }

    private void validateBookingConfirmation(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Only pending bookings can be confirmed");
        }
    }

    // ========== SEARCH METHODS IMPLEMENTATION ==========
    
    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> searchBookingsByCid(String cid, Long hotelId, Pageable pageable) {
        logger.info("Searching bookings by CID: {} for hotel: {}", cid, hotelId);
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
        
        if (cid == null || cid.trim().isEmpty()) {
            throw new IllegalArgumentException("CID cannot be null or empty");
        }
        
        try {
            Page<Booking> bookings = bookingRepository.findByCidAndHotelId(cid.trim(), hotelId, pageable);
            logger.debug("Found {} bookings for CID: {} in hotel: {}", bookings.getTotalElements(), cid, hotelId);
            return bookings.map(bookingMapper::toDto);
        } catch (Exception e) {
            logger.error("Error searching bookings by CID: {} for hotel: {}", cid, hotelId, e);
            throw new BusinessException("Failed to search bookings by CID: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> searchBookingsByPhone(String phone, Long hotelId, Pageable pageable) {
        logger.info("Searching bookings by phone: {} for hotel: {}", phone, hotelId);
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
        
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        
        try {
            Page<Booking> bookings = bookingRepository.findByPhoneAndHotelId(phone.trim(), hotelId, pageable);
            logger.debug("Found {} bookings for phone: {} in hotel: {}", bookings.getTotalElements(), phone, hotelId);
            return bookings.map(bookingMapper::toDto);
        } catch (Exception e) {
            logger.error("Error searching bookings by phone: {} for hotel: {}", phone, hotelId, e);
            throw new BusinessException("Failed to search bookings by phone: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> searchBookingsByCheckInDate(LocalDate checkInDate, Long hotelId, Pageable pageable) {
        logger.info("Searching bookings by check-in date: {} for hotel: {}", checkInDate, hotelId);
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
        
        if (checkInDate == null) {
            throw new IllegalArgumentException("Check-in date cannot be null");
        }
        
        try {
            Page<Booking> bookings = bookingRepository.findByCheckInDateAndHotelId(checkInDate, hotelId, pageable);
            logger.debug("Found {} bookings for check-in date: {} in hotel: {}", bookings.getTotalElements(), checkInDate, hotelId);
            return bookings.map(bookingMapper::toDto);
        } catch (Exception e) {
            logger.error("Error searching bookings by check-in date: {} for hotel: {}", checkInDate, hotelId, e);
            throw new BusinessException("Failed to search bookings by check-in date: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> searchBookingsByCheckOutDate(LocalDate checkOutDate, Long hotelId, Pageable pageable) {
        logger.info("Searching bookings by check-out date: {} for hotel: {}", checkOutDate, hotelId);
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
        
        if (checkOutDate == null) {
            throw new IllegalArgumentException("Check-out date cannot be null");
        }
        
        try {
            Page<Booking> bookings = bookingRepository.findByCheckOutDateAndHotelId(checkOutDate, hotelId, pageable);
            logger.debug("Found {} bookings for check-out date: {} in hotel: {}", bookings.getTotalElements(), checkOutDate, hotelId);
            return bookings.map(bookingMapper::toDto);
        } catch (Exception e) {
            logger.error("Error searching bookings by check-out date: {} for hotel: {}", checkOutDate, hotelId, e);
            throw new BusinessException("Failed to search bookings by check-out date: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> searchBookingsByStatus(String status, Long hotelId, Pageable pageable) {
        logger.info("Searching bookings by status: {} for hotel: {}", status, hotelId);
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
        
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        
        try {
            Page<Booking> bookings = bookingRepository.findByStatusAndHotelId(status.trim(), hotelId, pageable);
            logger.debug("Found {} bookings for status: {} in hotel: {}", bookings.getTotalElements(), status, hotelId);
            return bookings.map(bookingMapper::toDto);
        } catch (Exception e) {
            logger.error("Error searching bookings by status: {} for hotel: {}", status, hotelId, e);
            throw new BusinessException("Failed to search bookings by status: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> searchBookingsByDateRange(LocalDate startDate, LocalDate endDate, Long hotelId, Pageable pageable) {
        logger.info("Searching bookings by date range: {} to {} for hotel: {}", startDate, endDate, hotelId);
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
        validateDateRange(startDate, endDate);
        
        try {
            Page<Booking> bookings = bookingRepository.findByCheckInDateRange(hotelId, startDate, endDate, pageable);
            logger.debug("Found {} bookings in date range {} to {} for hotel: {}", 
                bookings.getTotalElements(), startDate, endDate, hotelId);
            return bookings.map(bookingMapper::toDto);
        } catch (Exception e) {
            logger.error("Error searching bookings by date range: {} to {} for hotel: {}", startDate, endDate, hotelId, e);
            throw new BusinessException("Failed to search bookings by date range: " + e.getMessage());
        }
    }
    


}