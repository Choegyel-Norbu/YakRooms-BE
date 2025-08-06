package com.yakrooms.be.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import com.yakrooms.be.dto.BookingStatisticsDTO;
import com.yakrooms.be.dto.MonthlyRevenueStatsDTO;
import com.yakrooms.be.dto.NotificationMessage;
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
import com.yakrooms.be.util.PasscodeGenerator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

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
                             Validator validator) {
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
    }

    @Override
    @CacheEvict(value = {"hotelBookings", "userBookings", "roomAvailability"}, allEntries = true)
    public BookingResponse createBooking(BookingRequest request) {
        logger.info("Creating booking for user: {}, room: {}, guests: {}", 
                   request.getUserId(), request.getRoomId(), request.getGuests());

        
        // Fetch entities with optimized queries
        User guest = fetchUserById(request.getUserId());
        Room room = fetchRoomById(request.getRoomId());
        Hotel hotel = fetchHotelById(request.getHotelId());
        
        logger.info("Room details - ID: {}, Number: {}, MaxGuests: {}, Type: {}", 
                   room.getId(), room.getRoomNumber(), room.getMaxGuests(), room.getRoomType());
        

        
        // Create booking entity
        Booking booking = createBookingEntity(request, guest, hotel, room);
        
        // Generate and set unique passcode
        booking.setPasscode(generateUniquePasscode());
        
        // Save booking
        Booking savedBooking = bookingRepository.save(booking);
        
        // Update room availability
        updateRoomAvailability(room, false);
        
        // Handle notifications and emails asynchronously
        handleBookingNotificationsAsync(savedBooking, guest, hotel, room);
        
        logger.info("Successfully created booking with ID: {}", savedBooking.getId());
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    @CacheEvict(value = {"hotelBookings", "userBookings", "roomAvailability"}, allEntries = true)
    public void deleteBookingById(Long bookingId) {
        logger.info("Deleting booking: {}", bookingId);
        
        Booking booking = fetchBookingById(bookingId);
        
        // Restore room availability if booking was active
        if (booking.isActive()) {
            updateRoomAvailability(booking.getRoom(), true);
        }
        
        bookingRepository.delete(booking);
        logger.info("Successfully deleted booking: {}", bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "bookingStats", key = "#startDate")
    public List<BookingStatisticsDTO> getBookingStatisticsByMonth(String startDate) {
        validateStartDate(startDate);
        LocalDateTime startDateTime = LocalDate.parse(startDate).atStartOfDay();
        return bookingRepository.getBookingStatisticsByMonth(startDateTime);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "hotelBookingStats", key = "#startDate + '_' + #hotelId")
    public List<BookingStatisticsDTO> getBookingStatisticsByMonthAndHotel(String startDate, Long hotelId) {
        validateStartDate(startDate);
        validateHotelId(hotelId);
        LocalDate startLocalDate = LocalDate.parse(startDate);
        return bookingRepository.getBookingStatisticsByMonthAndHotel(startLocalDate, hotelId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "revenueStats", key = "#hotelId + '_' + #startDate")
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
    @CacheEvict(value = {"hotelBookings", "userBookings", "roomAvailability"}, allEntries = true)
    public void cancelBooking(Long bookingId, Long userId) {
        logger.info("Cancelling booking: {} by user: {}", bookingId, userId);
        
        Booking booking = fetchBookingById(bookingId);
        validateBookingOwnership(booking, userId);
        validateBookingCancellation(booking);
        
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        // Restore room availability
        updateRoomAvailability(booking.getRoom(), true);
        
        logger.info("Successfully cancelled booking: {}", bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userBookings", key = "#userId")
    public List<BookingResponse> getUserBookings(Long userId) {
        validateUserId(userId);
        return bookingRepository.findAllBookingsByUserIdWithDetails(userId)
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "hotelBookings", key = "#hotelId")
    public List<BookingResponse> getBookingsByHotel(Long hotelId) {
        validateHotelId(hotelId);
        return bookingRepository.findAllByHotelIdOptimized(hotelId)
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = {"hotelBookings", "userBookings"}, allEntries = true)
    public BookingResponse confirmBooking(Long bookingId) {
        logger.info("Confirming booking: {}", bookingId);
        
        Booking booking = fetchBookingById(bookingId);
        validateBookingConfirmation(booking);
        
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(booking);
        
        logger.info("Successfully confirmed booking: {}", bookingId);
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "roomAvailability", key = "#roomId + '_' + #checkIn + '_' + #checkOut")
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        validateDateRange(checkIn, checkOut);
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(roomId, checkIn, checkOut);
        return conflictingBookings.isEmpty();
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
    public List<BookingResponse> listAllBookingNoPaginaiton() {
        return bookingRepository.findAll()
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> listAllBookingByHotel(Long hotelId, Pageable pageable) {
        validateHotelId(hotelId);
        return bookingRepository.findAllByHotelIdOptimized(hotelId, pageable)
                .map(bookingMapper::toDto);
    }

    @Override
    @CacheEvict(value = {"hotelBookings", "userBookings", "roomAvailability"}, allEntries = true)
    public boolean updateBookingStatus(Long bookingId, String newStatus) {
        logger.info("Updating booking status: {} to: {}", bookingId, newStatus);
        
        Booking booking = fetchBookingById(bookingId);
        BookingStatus status = validateAndParseStatus(newStatus);
        
        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(status);
        bookingRepository.save(booking);
        
        // Handle room availability based on status change
        handleRoomAvailabilityForStatusChange(booking, oldStatus, status);
        
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
        
        validateDateRange(request.getCheckInDate(), request.getCheckOutDate());
    }



    private Booking createBookingEntity(BookingRequest request, User guest, Hotel hotel, Room room) {
        Booking booking = bookingMapper.toEntityForCreation(request, guest, hotel, room);
        
        // Set check-in date to current date
        booking.setCheckInDate(LocalDate.now());
       
        return booking;
    }


    private void updateRoomAvailability(Room room, boolean available) {
        room.setAvailable(available);
        roomRepository.save(room);
        logger.debug("Updated room {} availability to: {}", room.getId(), available);
    }

    private void handleRoomAvailabilityForStatusChange(Booking booking, BookingStatus oldStatus, BookingStatus newStatus) {
        Room room = booking.getRoom();
        
        switch (newStatus) {
            case CHECKED_IN:
                if (oldStatus != BookingStatus.CHECKED_IN) {
                    updateRoomAvailability(room, false);
                }
                break;
            case CHECKED_OUT:
            case CANCELLED:
                if (oldStatus == BookingStatus.CHECKED_IN || oldStatus == BookingStatus.CONFIRMED) {
                    updateRoomAvailability(room, true);
                }
                break;
            default:
                // No room availability change needed for other statuses
                break;
        }
    }

    private void broadcastBookingStatusChange(Booking booking, BookingStatus oldStatus, BookingStatus newStatus) {
        try {
            // Create booking change event
            BookingChangeEvent event = new BookingChangeEvent(
                booking.getId(),
                booking.getHotel().getId(),
                booking.getUser().getId(),
                oldStatus,
                newStatus,
                "BOOKING_STATUS_CHANGE",
                String.format("Booking status changed from %s to %s", oldStatus, newStatus)
            );
            
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
                sendGuestPasscodeEmail(booking, guest, hotel, room);
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
                
                // Send email notification
                mailService.sendBookingNotificationEmail(
                    hotelAdmin.getEmail(), 
                    hotel.getName(), 
                    room.getRoomNumber(), 
                    guest.getName()
                );
                
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
        dbNotification.setMessage(String.format("Booking for Room %s by %s", room.getRoomNumber(), guest.getName()));
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
                booking.getCheckOutDate()
            );
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
        throw new BusinessException("Unable to generate unique passcode after " + MAX_PASSCODE_ATTEMPTS + " attempts. Please try again later.");
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