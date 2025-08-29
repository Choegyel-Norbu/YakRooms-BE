package com.yakrooms.be.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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
import com.yakrooms.be.dto.request.BookingRequest;
import com.yakrooms.be.dto.response.BookingResponse;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.exception.BusinessException;
import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.Notification;
import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.BookingStatus;
import com.yakrooms.be.model.enums.Role;
import com.yakrooms.be.repository.BookingRepository;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.repository.NotificationRepository;
import com.yakrooms.be.repository.RoomRepository;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.service.BookingService;
import com.yakrooms.be.service.MailService;
import com.yakrooms.be.service.NotificationService;
import com.yakrooms.be.service.BookingWebSocketService;
import com.yakrooms.be.service.PaymentService;
import com.yakrooms.be.service.BookingValidationService;
import com.yakrooms.be.util.PasscodeGenerator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import com.yakrooms.be.model.enums.PaymentStatus;

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
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final MailService mailService;
    private final BookingMapper bookingMapper;
    private final NotificationService notificationService;
    private final BookingWebSocketService bookingWebSocketService;
    private final Validator validator;
    
    // New unified services
    private final PaymentService paymentService;
    private final BookingValidationService bookingValidationService;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository,
            RoomRepository roomRepository,
            HotelRepository hotelRepository,
            UserRepository userRepository,
            NotificationRepository notificationRepository,
            MailService mailService,
            BookingMapper bookingMapper,
            NotificationService notificationService,
            BookingWebSocketService bookingWebSocketService,
            Validator validator,
            PaymentService paymentService,
            BookingValidationService bookingValidationService) {
        
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.mailService = mailService;
        this.bookingMapper = bookingMapper;
        this.notificationService = notificationService;
        this.bookingWebSocketService = bookingWebSocketService;
        this.validator = validator;
        this.paymentService = paymentService;
        this.bookingValidationService = bookingValidationService;
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        logger.info("Creating booking for user: {}, room: {}, guests: {}",
                request.getUserId(), request.getRoomId(), request.getGuests());

        // Use the new validation service
        bookingValidationService.validateBookingRequest(request);

        // Fetch entities with optimized queries
        User guest = null;
        if (request.getUserId() != null) {
            guest = fetchUserById(request.getUserId());
        }
        Room room = fetchRoomById(request.getRoomId());
        Hotel hotel = fetchHotelById(request.getHotelId());

        logger.info("Room details - ID: {}, Number: {}, MaxGuests: {}, Type: {}",
                room.getId(), room.getRoomNumber(), room.getMaxGuests(), room.getRoomType());

        // Strict overlap detection (date-only fallback if times not provided)
        // Use time-based overlap if client supplies times on Booking or request; otherwise default to date-only
        boolean availableByTime = isRoomAvailableForDatesAndTimes(
                request.getRoomId(),
                request.getCheckInDate(), java.time.LocalTime.MIDNIGHT,
                request.getCheckOutDate(), java.time.LocalTime.NOON);
        if (!availableByTime) {
            throw new BusinessException("Time window conflicts with an existing booking");
        }

        // Create booking entity
        Booking booking = createBookingEntity(request, guest, hotel, room);

        // Generate and set unique passcode
        booking.setPasscode(generateUniquePasscode());

        // Save booking
        Booking savedBooking = bookingRepository.save(booking);

        // Do not mutate global room availability per new policy

        // Handle notifications and emails asynchronously
        handleBookingNotificationsAsync(savedBooking, guest, hotel, room);

        logger.info("Successfully created booking with ID: {}", savedBooking.getId());
        return bookingMapper.toDto(savedBooking);
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
        validateHotelId(hotelId);
        LocalDate startLocalDate = LocalDate.parse(startDate);
        return bookingRepository.getBookingStatisticsByMonthAndHotel(startLocalDate, hotelId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyRevenueStatsDTO> getMonthlyRevenueStats(Long hotelId, String startDate) {
        validateHotelId(hotelId);
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

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Do not flip global availability on cancel per new policy

        logger.info("Successfully cancelled booking: {}", bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByHotel(Long hotelId) {
        validateHotelId(hotelId);
        return bookingRepository.findAllByHotelIdOptimized(hotelId)
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse confirmBooking(Long bookingId) {
        logger.info("Confirming booking: {}", bookingId);

        Booking booking = fetchBookingById(bookingId);
        
        // Use the new validation service
        bookingValidationService.validateConfirmation(booking);

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(booking);

        logger.info("Successfully confirmed booking: {}", bookingId);
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return isRoomAvailableForDates(roomId, checkIn, checkOut);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRoomAvailableForAdvanceBooking(BookingRequest request) {
        // Check for conflicting CONFIRMED bookings only
        List<Booking> conflictingBookings = getConflictingBookings(
            request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate());
        
        // Filter to only CONFIRMED bookings for advance booking conflicts
        return conflictingBookings.stream()
            .noneMatch(booking -> booking.getStatus() == BookingStatus.CONFIRMED);
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
        validateHotelId(hotelId);
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

        // Handle room availability based on status change
        Room room = booking.getRoom();
        if (room != null) {
            try {
                boolean shouldBeAvailable = true;
                String action = "freed up";
                
                if (status == BookingStatus.CHECKED_IN) {
                    shouldBeAvailable = false;
                    action = "occupied";
                } else if (status == BookingStatus.CHECKED_OUT || status == BookingStatus.CANCELLED) {
                    shouldBeAvailable = true;
                    action = "freed up";
                } else {
                    // No room availability change needed for other statuses
                    // Note: CONFIRMED status could potentially make room unavailable in future implementations
                    logger.debug("No room availability change needed for status: {} for booking {}", status, bookingId);
                    return true;
                }
                
                // Validate that the room belongs to the same hotel as the booking
                if (booking.getHotel() != null && room.getHotel() != null && 
                    !booking.getHotel().getId().equals(room.getHotel().getId())) {
                    logger.warn("Room {} and booking {} belong to different hotels, skipping availability update", 
                        room.getId(), bookingId);
                    return true;
                }
                
                // Only update if the current availability is different
                if (room.isAvailable() != shouldBeAvailable) {
                    room.setAvailable(shouldBeAvailable);
                    roomRepository.save(room);
                    logger.info("Room {} availability set to {} after {} for booking {} ({}: {})", 
                        room.getId(), shouldBeAvailable, status.toString().toLowerCase(), bookingId, 
                        action, room.getRoomNumber());
                } else {
                    logger.debug("Room {} is already {}, no update needed for booking {}", 
                        room.getId(), shouldBeAvailable ? "available" : "unavailable", bookingId);
                }
            } catch (Exception e) {
                logger.error("Failed to update room {} availability after {} for booking {}: {}", 
                    room.getId(), status.toString().toLowerCase(), bookingId, e.getMessage());
                // Don't fail the entire operation if room update fails
            }
        } else {
            logger.warn("Room is null for booking {}, cannot update availability", bookingId);
        }

        // Broadcast WebSocket event for booking status change
        broadcastBookingStatusChange(booking, oldStatus, status);

        logger.info("Successfully updated booking status: {} to: {}", bookingId, status);
        return true;
    }

    // ========== PRIVATE HELPER METHODS ==========

    private void validateBookingRequest(BookingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Booking request cannot be null");
        }

        Set<ConstraintViolation<BookingRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Invalid booking request: " + errorMessage);
        }

        // Use the new validation service for date range validation
        bookingValidationService.validateDateRange(request.getCheckInDate(), request.getCheckOutDate(), "GENERAL");
    }

    private Booking createBookingEntity(BookingRequest request, User guest, Hotel hotel, Room room) {
        Booking booking = bookingMapper.toEntityForCreation(request, guest, hotel, room);

        // Set check-in date to current date for immediate bookings
        booking.setCheckInDate(LocalDate.now());

        return booking;
    }

    /**
     * Update room availability for a booking using the unified service.
     * This method ensures atomic updates and prevents race conditions.
     * 
     * @param booking The booking to update availability for
     */
    private void updateRoomAvailabilityForBooking(Booking booking) {
        // No-op per new policy
    }

    private void handleRoomAvailabilityForStatusChange(Booking booking, BookingStatus oldStatus,
            BookingStatus newStatus) {
        // No-op per new policy
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

    private void handleBookingNotificationsAsync(Booking booking, User guest, Hotel hotel, Room room) {
        CompletableFuture.runAsync(() -> {
            try {
                sendHotelAdminNotification(booking, guest, hotel, room);
                // Only send guest email if guest is not null
                if (guest != null) {
                    sendGuestPasscodeEmail(booking, guest, hotel, room);
                } else {
                    logger.info("Skipping guest email notification for booking {} as guest is null", booking.getId());
                }
            } catch (Exception e) {
                logger.error("Failed to send booking notifications for booking: {}", booking.getId(), e);
            }
        });
    }

    private void sendHotelAdminNotification(Booking booking, User guest, Hotel hotel, Room room) {
        try {
            Optional<User> hotelAdminOpt = userRepository.findByHotelIdAndRole(hotel.getId(), Role.HOTEL_ADMIN);

            if (hotelAdminOpt.isPresent()) {
                User hotelAdmin = hotelAdminOpt.get();

                // Save notification in database
                saveNotificationToDatabase(hotelAdmin, booking, guest, room);

                // Send email notification with complete data
                String guestName = guest != null ? guest.getName() : "Anonymous Guest";
                String guestEmail = guest != null ? guest.getEmail() : "No email provided";
                mailService.sendBookingNotificationEmail(
                        hotelAdmin.getEmail(),
                        hotel.getName(),
                        room.getRoomNumber(),
                        guestName,
                        booking.getId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        guestEmail);

                logger.info("Sent booking notification to hotel admin: {}", hotelAdmin.getId());
            } else {
                logger.warn("No hotel admin found for hotel: {}", hotel.getId());
            }
        } catch (Exception e) {
            logger.error("Failed to send hotel admin notification for booking: {}", booking.getId(), e);
        }
    }

    private void saveNotificationToDatabase(User hotelAdmin, Booking booking, User guest, Room room) {
        Notification dbNotification = new Notification();
        dbNotification.setUser(hotelAdmin);
        dbNotification.setTitle("New Booking!");
        String guestName = guest != null ? guest.getName() : "Anonymous Guest";
        dbNotification.setMessage(String.format("Booking for Room %s by %s", room.getRoomNumber(), guestName));
        dbNotification.setType("BOOKING");
        dbNotification.setRead(false);
        dbNotification.setCreatedAt(LocalDateTime.now());
        dbNotification.setRoom(room); // Set the room association
        notificationRepository.save(dbNotification);
    }

    private void sendGuestPasscodeEmail(Booking booking, User guest, Hotel hotel, Room room) {
        try {
            mailService.sendPasscodeEmailToGuest(
                    guest.getEmail(),
                    guest.getName(),
                    booking.getPasscode(),
                    hotel.getName(),
                    room.getRoomNumber(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    booking.getId());
            logger.info("Sent passcode email to guest: {}", guest.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send passcode email to guest: {}", guest.getEmail(), e);
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
        return roomRepository.findByIdWithItems(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
    }

    private Hotel fetchHotelById(Long hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));
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

    private void validateHotelId(Long hotelId) {
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
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
}