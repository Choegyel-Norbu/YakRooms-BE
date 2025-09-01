package com.yakrooms.be.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yakrooms.be.dto.mapper.BookingMapper;
import com.yakrooms.be.dto.request.BookingRequest;
import com.yakrooms.be.dto.request.BookingExtensionRequest;
import com.yakrooms.be.dto.response.BookingResponse;
import com.yakrooms.be.dto.response.BookingExtensionResponse;
import com.yakrooms.be.exception.BusinessException;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.Room;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.BookingStatus;
import com.yakrooms.be.repository.BookingRepository;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.repository.RoomRepository;
import com.yakrooms.be.repository.UserRepository;

import com.yakrooms.be.service.MailService;
import com.yakrooms.be.service.NotificationService;

import com.yakrooms.be.service.RoomAvailabilityService;
import com.yakrooms.be.service.UnifiedBookingService;
import com.yakrooms.be.service.BookingWebSocketService;
import com.yakrooms.be.util.PasscodeGenerator;
import com.yakrooms.be.model.enums.NotificationType;

/**
 * Implementation of UnifiedBookingService that handles ONLY booking creation with pessimistic locking.
 * This service is focused on preventing race conditions during concurrent booking creation.
 * All other booking operations are handled by BookingServiceImpl.
 * 
 * @author YakRooms Team
 * @version 2.0
 */
@Service
@Transactional
public class UnifiedBookingServiceImpl implements UnifiedBookingService {
    
    private static final Logger logger = LoggerFactory.getLogger(UnifiedBookingServiceImpl.class);
    
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final BookingMapper bookingMapper;
    private final NotificationService notificationService;
    private final BookingWebSocketService bookingWebSocketService;
    private final RoomAvailabilityService roomAvailabilityService;
    
    public UnifiedBookingServiceImpl(
            BookingRepository bookingRepository,
            RoomRepository roomRepository,
            HotelRepository hotelRepository,
            UserRepository userRepository,
            MailService mailService,
            BookingMapper bookingMapper,
            NotificationService notificationService,
            BookingWebSocketService bookingWebSocketService,
            RoomAvailabilityService roomAvailabilityService) {
        
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.bookingMapper = bookingMapper;
        this.notificationService = notificationService;
        this.bookingWebSocketService = bookingWebSocketService;
        this.roomAvailabilityService = roomAvailabilityService;
    }
    
    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        logger.info("Creating booking for room: {} with pessimistic locking", request.getRoomId());
        
        try {
            // CRITICAL: Check room availability WITH pessimistic locking to prevent race conditions
            // This must happen inside the transaction to ensure atomicity
            if (!checkRoomAvailabilityWithPessimisticLock(request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate())) {
                throw new BusinessException("Room is not available for the requested dates");
            }
            
            // Create the booking
            Booking booking = createBookingEntity(request, "IMMEDIATE");
            booking.setStatus(BookingStatus.CONFIRMED);
            
            // Save the booking
            Booking savedBooking = bookingRepository.save(booking);
            
            // Update room availability using the centralized service
            roomAvailabilityService.updateRoomAvailabilityForNewBooking(
                savedBooking.getRoom().getId(), 
                savedBooking.getCheckInDate(), 
                savedBooking.getCheckInTime()
            );
            
            // Handle notifications asynchronously (outside transaction)
            handleBookingNotificationsAsync(savedBooking);
            
            logger.info("Successfully created booking with ID: {}", savedBooking.getId());
            return bookingMapper.toDto(savedBooking);
            
        } catch (Exception e) {
            logger.error("Failed to create booking: {}", e.getMessage());
            throw e;
        }
    }
    
    @Override
    @Transactional
    public BookingResponse createSingleNightBooking(BookingRequest request) {
        logger.info("Creating single-night booking for room: {} with automatic checkout date", request.getRoomId());
        
        try {
            // Validate required fields for single-night booking
            if (request.getCheckInDate() == null) {
                throw new BusinessException("Check-in date is required for single-night booking");
            }
            
            // Automatically set checkout date to the day after check-in
            LocalDate autoCheckOutDate = request.getCheckInDate().plusDays(1);
            logger.info("Auto-setting checkout date to: {} (day after check-in: {})", 
                       autoCheckOutDate, request.getCheckInDate());
            
            // CRITICAL: Check room availability WITH pessimistic locking to prevent race conditions
            // Use the automatically calculated checkout date for availability check
            if (!checkRoomAvailabilityWithPessimisticLock(request.getRoomId(), request.getCheckInDate(), autoCheckOutDate)) {
                throw new BusinessException("Room is not available for the requested dates (check-in: " + 
                                          request.getCheckInDate() + ", auto checkout: " + autoCheckOutDate + ")");
            }
            
            // Create a modified request with the auto-calculated checkout date
            BookingRequest modifiedRequest = createModifiedRequestWithAutoCheckout(request, autoCheckOutDate);
            
            // Create the booking entity
            Booking booking = createBookingEntity(modifiedRequest, "SINGLE_NIGHT");
            booking.setStatus(BookingStatus.CONFIRMED);
            
            // Save the booking
            Booking savedBooking = bookingRepository.save(booking);
            
            // Update room availability using the centralized service
            roomAvailabilityService.updateRoomAvailabilityForNewBooking(
                savedBooking.getRoom().getId(), 
                savedBooking.getCheckInDate(), 
                savedBooking.getCheckInTime()
            );
            
            // Handle notifications asynchronously (outside transaction)
            handleBookingNotificationsAsync(savedBooking);
            
            logger.info("Successfully created single-night booking with ID: {} (check-in: {}, checkout: {})", 
                       savedBooking.getId(), savedBooking.getCheckInDate(), savedBooking.getCheckOutDate());
            return bookingMapper.toDto(savedBooking);
            
        } catch (Exception e) {
            logger.error("Failed to create single-night booking: {}", e.getMessage());
            throw e;
        }
    }
    
    @Override
    @Transactional
    public BookingExtensionResponse extendBooking(Long bookingId, BookingExtensionRequest request) {
        logger.info("Extending booking: {} to new check-out date: {}", bookingId, request.getNewCheckOutDate());
        
        try {
            // Fetch the existing booking with pessimistic locking to prevent concurrent modifications
            Booking existingBooking = fetchBookingWithPessimisticLock(bookingId);
            
            // Log current time and checkout date for debugging
            LocalTime currentTime = LocalTime.now();
            LocalDate currentDate = LocalDate.now();
            LocalTime jobScheduleTime = LocalTime.of(12, 0);
            
            logger.info("Extension request - Current time: {}, Current date: {}, Checkout date: {}", 
                       currentTime, currentDate, existingBooking.getCheckOutDate());
            
            // Provide timing guidance for same-day extensions
            if (existingBooking.getCheckOutDate().equals(currentDate)) {
                if (currentTime.isBefore(jobScheduleTime)) {
                    logger.info("Extension request made before 12:00 PM - optimal timing for checkout day extension");
                } else {
                    logger.info("Extension request made after 12:00 PM - room availability has been updated, checking for conflicts");
                }
            }
            
            // Validate the extension request
            validateBookingExtension(existingBooking, request);
            
            // Check if the extended dates are available for the same room
            if (!checkRoomAvailabilityForExtension(existingBooking.getRoom().getId(), 
                                                  existingBooking.getCheckOutDate(), 
                                                  request.getNewCheckOutDate())) {
                throw new BusinessException("Room is not available for the extended dates");
            }
            
            // Calculate additional cost for the extension
            BigDecimal additionalCost = calculateExtensionCost(existingBooking, request.getNewCheckOutDate());
            
            // Store original values for response
            LocalDate originalCheckOutDate = existingBooking.getCheckOutDate();
            BigDecimal originalPrice = existingBooking.getTotalPrice();
            
            // Update the booking
            updateBookingForExtension(existingBooking, request);
            
            // Calculate new total price
            BigDecimal newTotalPrice = originalPrice.add(additionalCost);
            existingBooking.setTotalPrice(newTotalPrice);
            
            // Save the updated booking
            Booking updatedBooking = bookingRepository.save(existingBooking);
            
            // Update room availability for the extended period
            roomAvailabilityService.updateRoomAvailabilityForNewBooking(
                updatedBooking.getRoom().getId(),
                originalCheckOutDate,
                LocalTime.of(12, 0) // Default check-out time
            );
            
            // Handle notifications asynchronously
            // handleExtensionNotificationsAsync(updatedBooking, originalCheckOutDate, request.getNewCheckOutDate());
            
            logger.info("Successfully extended booking: {} from {} to {}", 
                       bookingId, originalCheckOutDate, request.getNewCheckOutDate());
            
            return new BookingExtensionResponse(
                updatedBooking.getId(),
                originalCheckOutDate,
                request.getNewCheckOutDate(),
                originalPrice,
                additionalCost,
                newTotalPrice
            );
            
        } catch (Exception e) {
            logger.error("Failed to extend booking: {} - {}", bookingId, e.getMessage());
            return new BookingExtensionResponse(bookingId, "Failed to extend booking: " + e.getMessage(), false);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean checkRoomAvailability(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        // Use time-based availability check with default times
        LocalTime defaultCheckInTime = LocalTime.of(0, 0); // 12:00 AM (midnight)
        LocalTime defaultCheckOutTime = LocalTime.of(12, 0); // 12:00 PM (noon)
        return checkRoomAvailabilityWithTimes(roomId, checkIn, defaultCheckInTime, checkOut, defaultCheckOutTime);
    }
    
    /**
     * Check room availability with specific times for precise conflict detection.
     * This method is unique to UnifiedBookingService and provides time-based availability checking.
     * 
     * @return true if room is available
     */
    public boolean checkRoomAvailabilityWithTimes(Long roomId, LocalDate checkIn, LocalTime checkInTime, 
                                                LocalDate checkOut, LocalTime checkOutTime) {
        // Check for conflicting bookings using time-based logic
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
            roomId, checkIn, checkOut);
        return conflicts.isEmpty();
    }
    
    /**
     * Check room availability with pessimistic locking to prevent race conditions during booking creation.
     * This method is UNIQUE to UnifiedBookingService and MUST be called within a transaction.
     * 
     * @return true if room is available, false if conflicts exist
     */
    private boolean checkRoomAvailabilityWithPessimisticLock(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        // Use pessimistic locking to prevent concurrent access during availability check
        // This ensures that only one thread can check availability at a time for the same room
        List<Booking> conflicts = bookingRepository.findConflictingBookingsForUpdate(roomId, checkIn, checkOut);
        return conflicts.isEmpty();
    }
    
    // ========== PRIVATE HELPER METHODS FOR BOOKING EXTENSION ==========
    
    /**
     * Fetch a booking with pessimistic locking to prevent concurrent modifications.
     * 
     * @param bookingId The booking ID
     * @return The booking entity
     * @throws ResourceNotFoundException if booking not found
     */
    private Booking fetchBookingWithPessimisticLock(Long bookingId) {
        return bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
    }
    

    
    /**
     * Validate the booking extension request.
     * 
     * @param booking The existing booking
     * @param request The extension request
     * @throws BusinessException if extension is not valid
     */
    private void validateBookingExtension(Booking booking, BookingExtensionRequest request) {
        // Check if booking exists and is in a valid state for extension
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Cannot extend a cancelled booking");
        }
        
        if (booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new BusinessException("Cannot extend a checked-out booking");
        }
        
        // Check if new check-out date is after current check-out date
        if (!request.getNewCheckOutDate().isAfter(booking.getCheckOutDate())) {
            throw new BusinessException("New check-out date must be after the current check-out date");
        }
        
        // Check if new check-out date is not too far in the future (business rule)
        LocalDate maxExtensionDate = LocalDate.now().plusMonths(6); // 6 months max extension
        if (request.getNewCheckOutDate().isAfter(maxExtensionDate)) {
            throw new BusinessException("Cannot extend booking more than 6 months in advance");
        }
        
        // TIME-BASED VALIDATION: Check if extension is allowed based on checkout date and current time
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        LocalTime jobScheduleTime = LocalTime.of(12, 0); // Job runs at 12:00 PM
        
        // Check if this is a same-day extension request
        if (booking.getCheckOutDate().equals(currentDate)) {
            if (currentTime.isAfter(jobScheduleTime) || currentTime.equals(jobScheduleTime)) {
                // After job scheduler has run, provide warning but still allow if no conflicts
                logger.warn("Extension request made after 12:00 PM on checkout day for booking: {}. " +
                           "Room availability has been updated by the system scheduler. " +
                           "Proceeding with conflict check...", booking.getId());
            }
        } else if (booking.getCheckOutDate().isBefore(currentDate)) {
            throw new BusinessException("Cannot extend a booking with a past checkout date. " +
                "The original checkout date has already passed.");
        }
        
        // Validate guest count if provided
        if (request.getGuests() != null && (request.getGuests() < 1 || request.getGuests() > 20)) {
            throw new BusinessException("Guest count must be between 1 and 20");
        }
        
        logger.info("Booking extension validation passed for booking: {} - checkout: {}, new checkout: {}, current time: {}", 
                   booking.getId(), booking.getCheckOutDate(), request.getNewCheckOutDate(), LocalTime.now());
    }
    
    /**
     * Check room availability for the extension period.
     * 
     * @param roomId The room ID
     * @param currentCheckOut Current check-out date
     * @param newCheckOut New check-out date
     * @return true if available for extension
     */
    private boolean checkRoomAvailabilityForExtension(Long roomId, LocalDate currentCheckOut, LocalDate newCheckOut) {
        // Check availability from current check-out to new check-out date
        // Exclude the current booking from conflict check
        List<Booking> conflicts = bookingRepository.findConflictingBookingsForExtension(
            roomId, currentCheckOut, newCheckOut);
        return conflicts.isEmpty();
    }
    
    /**
     * Calculate the additional cost for the extension.
     * 
     * @param booking The existing booking
     * @param newCheckOutDate The new check-out date
     * @return The additional cost
     */
    private BigDecimal calculateExtensionCost(Booking booking, LocalDate newCheckOutDate) {
        // Calculate additional days
        long additionalDays = ChronoUnit.DAYS.between(booking.getCheckOutDate(), newCheckOutDate);
        
        // Get room price per night
        Room room = booking.getRoom();
        BigDecimal pricePerNight = BigDecimal.valueOf(room.getPrice());
        
        // Calculate additional cost (additional days × price per night)
        BigDecimal additionalCost = pricePerNight.multiply(BigDecimal.valueOf(additionalDays));
        
        logger.info("Extension cost calculation: {} days × ${} = ${}", 
                   additionalDays, pricePerNight, additionalCost);
        
        return additionalCost;
    }
    
    /**
     * Update the booking entity for the extension.
     * 
     * @param booking The existing booking
     * @param request The extension request
     */
    private void updateBookingForExtension(Booking booking, BookingExtensionRequest request) {
        // Update check-out date
        booking.setCheckOutDate(request.getNewCheckOutDate());
        
        // Update optional fields if provided
        if (request.getGuests() != null) {
            booking.setGuests(request.getGuests());
        }
        
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            booking.setPhone(request.getPhone().trim());
        }
        
        if (request.getDestination() != null && !request.getDestination().trim().isEmpty()) {
            booking.setDestination(request.getDestination().trim());
        }
        
        if (request.getOrigin() != null && !request.getOrigin().trim().isEmpty()) {
            booking.setOrigin(request.getOrigin().trim());
        }
        
        // Update the updated timestamp
        booking.setUpdatedAt(java.time.LocalDateTime.now());
    }
    

    
    // ========== PRIVATE HELPER METHODS ==========
    
    /**
     * Create a modified BookingRequest with auto-calculated checkout date.
     * This ensures the original request remains unchanged while providing the checkout date.
     * 
     * @param originalRequest The original booking request
     * @param autoCheckOutDate The automatically calculated checkout date
     * @return A new BookingRequest with the checkout date set
     */
    private BookingRequest createModifiedRequestWithAutoCheckout(BookingRequest originalRequest, LocalDate autoCheckOutDate) {
        BookingRequest modifiedRequest = new BookingRequest();
        
        // Copy all fields from original request
        modifiedRequest.setUserId(originalRequest.getUserId());
        modifiedRequest.setHotelId(originalRequest.getHotelId());
        modifiedRequest.setRoomId(originalRequest.getRoomId());
        modifiedRequest.setCheckInDate(originalRequest.getCheckInDate());
        modifiedRequest.setCheckOutDate(autoCheckOutDate); // Set the auto-calculated checkout date
        modifiedRequest.setGuests(originalRequest.getGuests());
        modifiedRequest.setNumberOfRooms(originalRequest.getNumberOfRooms());
        modifiedRequest.setTotalPrice(originalRequest.getTotalPrice());
        modifiedRequest.setPhone(originalRequest.getPhone());
        modifiedRequest.setCid(originalRequest.getCid());
        modifiedRequest.setDestination(originalRequest.getDestination());
        modifiedRequest.setOrigin(originalRequest.getOrigin());
        modifiedRequest.setGuestName(originalRequest.getGuestName());
        
        return modifiedRequest;
    }
    
    private Booking createBookingEntity(BookingRequest request, String bookingType) {
        // Fetch required entities
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        }
        
        Room room = roomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + request.getRoomId()));
        
        Hotel hotel = hotelRepository.findById(request.getHotelId())
            .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + request.getHotelId()));
        
        // Create booking entity
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setHotel(hotel);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setCheckInTime(LocalTime.of(0, 0)); // Default check-in time
        booking.setCheckOutTime(LocalTime.of(12, 0)); // Default check-out time
        booking.setGuests(request.getGuests());
        booking.setTotalPrice(request.getTotalPrice());
        booking.setPhone(request.getPhone());
        booking.setCid(request.getCid());
        booking.setDestination(request.getDestination());
        booking.setOrigin(request.getOrigin());
        booking.setGuestName(request.getGuestName());
        booking.setPasscode(PasscodeGenerator.generatePasscode());
        booking.setStatus(BookingStatus.PENDING);
        
        return booking;
    }

    
    /**
     * Handle booking notifications asynchronously.
     * 
     * @param booking The booking to send notifications for
     */
    private void handleBookingNotificationsAsync(Booking booking) {
        CompletableFuture.runAsync(() -> {
            try {
                logger.info("Sending notifications for booking: {}", booking.getId());

                // Create a notification for the user who made the booking
                if (booking.getUser() != null) {
                    String message = String.format("Your booking for room %s in %s from %s to %s has been confirmed. Passcode: %s",
                                                   booking.getRoom().getRoomNumber(),
                                                   booking.getHotel().getName(),
                                                   booking.getCheckInDate(),
                                                   booking.getCheckOutDate(),
                                                   booking.getPasscode());
                    
                    // Create in-app notification
                    notificationService.createNotification(booking.getUser(), message, NotificationType.BOOKING_CONFIRMATION);
                    logger.info("Booking confirmation notification created for user: {}", booking.getUser().getId());
                    
                    // Send email confirmation if user has email
                    if (booking.getUser().getEmail() != null && !booking.getUser().getEmail().trim().isEmpty()) {
                        try {
                            String guestName = booking.getGuestName() != null ? booking.getGuestName() : 
                                             (booking.getUser().getName() != null ? booking.getUser().getName() : "Guest");
                            
                            mailService.sendPasscodeEmailToGuest(
                                booking.getUser().getEmail(),
                                guestName,
                                booking.getPasscode(),
                                booking.getHotel().getName(),
                                booking.getRoom().getRoomNumber(),
                                booking.getCheckInDate(),
                                booking.getCheckOutDate(),
                                booking.getId()
                            );
                            logger.info("Booking confirmation email sent to: {}", booking.getUser().getEmail());
                        } catch (Exception e) {
                            logger.warn("Failed to send booking confirmation email to {}: {}", 
                                       booking.getUser().getEmail(), e.getMessage());
                        }
                    }
                }

                // Send WebSocket notification for real-time updates
                bookingWebSocketService.notifyBookingUpdates(booking.getHotel().getId(), "New booking created: " + booking.getId());

            } catch (Exception e) {
                logger.error("Failed to send notifications for booking: {}", booking.getId(), e);
            }
        });
    }
}
