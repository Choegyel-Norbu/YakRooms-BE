package com.yakrooms.be.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yakrooms.be.dto.BookingStatisticsDTO;
import com.yakrooms.be.dto.MonthlyRevenueStatsDTO;
import com.yakrooms.be.dto.NotificationMessage;
import com.yakrooms.be.dto.PasscodeVerificationDTO;
import com.yakrooms.be.util.PasscodeGenerator;
import com.yakrooms.be.dto.mapper.BookingMapper;
import com.yakrooms.be.dto.request.BookingRequest;
import com.yakrooms.be.dto.response.BookingResponse;
import com.yakrooms.be.exception.ResourceNotFoundException;
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
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private MailService mailService;
    
    private final BookingMapper bookingMapper;
    
    private final NotificationService notificationService; // <-- Inject the service


    @Autowired
    public BookingServiceImpl(BookingMapper bookingMapper, NotificationService notificationService) {
        this.bookingMapper = bookingMapper;
		this.notificationService = notificationService;
    }

    @Override
    public BookingResponse createBooking(BookingRequest request) {
        User guest = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        Booking booking = bookingMapper.toEntityForCreation(request, guest, hotel, room);
        
        // Generate unique passcode for the booking
        String passcode = generateUniquePasscode();
        booking.setPasscode(passcode);
        
        Booking savedBooking = bookingRepository.save(booking);

        // Update room availability to false when booking is created
        room.setAvailable(false);
        roomRepository.save(room);

        // --- START: NOTIFICATION LOGIC ---

        // Assuming your Hotel entity has a relationship to its owner (User).
        User hotelAdmin = userRepository.findByHotelIdAndRole(request.getHotelId(), Role.HOTEL_ADMIN)
        	    .orElse(null);

        // Get hotel admin
        System.out.println("=== NOTIFICATION DEBUG ===");
        System.out.println("Hotel ID: " + hotel.getId());
        System.out.println("Hotel Owner: " + hotelAdmin);
        System.out.println("Hotel Owner ID: " + (hotelAdmin != null ? hotelAdmin.getId() : "NULL"));
        
        if (hotelAdmin != null && hotelAdmin.getId() != null) {
            try {
                NotificationMessage notification = new NotificationMessage(
                    "New Booking!",
                    String.format("New booking for Room %s by %s", room.getRoomNumber(), guest.getName()),
                    "BOOKING"
                );
                System.out.println("Sending notification: " + notification);
                notificationService.notifyUser(String.valueOf(hotelAdmin.getId()), notification);
                System.out.println("Notification sent to userId: " + hotelAdmin.getId());

                // Save notification in the database
                com.yakrooms.be.model.entity.Notification dbNotification = new com.yakrooms.be.model.entity.Notification();
                dbNotification.setUser(hotelAdmin);
                dbNotification.setTitle("New Booking!");
                dbNotification.setMessage(String.format("Booking for Room %s by %s", room.getRoomNumber(), guest.getName()));
                dbNotification.setType("BOOKING");
                dbNotification.setRead(false);
                dbNotification.setCreatedAt(java.time.LocalDateTime.now());
                notificationRepository.save(dbNotification);

                // Send email notification asynchronously
                CompletableFuture.runAsync(() -> {
                    try {
                        mailService.sendBookingNotificationEmail(hotelAdmin.getEmail(), hotel.getName(), room.getRoomNumber(), guest.getName());
                    } catch (Exception e) {
                        System.err.println("Failed to send email notification: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                System.err.println("Failed to send notification: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("No hotel admin found!");
        }

        // --- END: NOTIFICATION LOGIC ---

        return bookingMapper.toDto(savedBooking);
    }

    @Override
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only cancel your own bookings");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Set room availability back to true when booking is cancelled
        Room room = booking.getRoom();
        if (room != null) {
            room.setAvailable(true);
            roomRepository.save(room);
        }
    }

    @Override
    public List<BookingResponse> getUserBookings(Long userId) {
        return bookingRepository.findAllByUserId(userId).stream().map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getBookingsByHotel(Long hotelId) {
        return bookingRepository.findAllByHotelId(hotelId).stream().map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        List<Booking> bookings = bookingRepository.findBookingsForRoom(roomId, checkIn, checkOut);
        return bookings.isEmpty();
    }

    @Override
    public BookingResponse getBookingDetails(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        return bookingMapper.toDto(booking);
    }

    @Override
    public List<BookingResponse> listAllBookingNoPaginaiton() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> listAllBooking(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(bookingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> listAllBookingByHotel(Long hotelId, Pageable pageable) {
        return bookingRepository.findAllByHotelId(hotelId, pageable).map(bookingMapper::toDto);
    }

    @Override
    public boolean updateBookingStatus(Long bookingId, String newStatus) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            throw new ResourceNotFoundException("Booking not found with ID: " + bookingId);
        }

        Booking booking = optionalBooking.get();

        // Normalize and validate status input
        String normalizedStatus = newStatus.trim().toUpperCase();

        // Optional: Define allowed status values (or compare to BookingStatus enum values)
        Set<String> allowedStatuses = Set.of("PENDING", "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED");

        if (!allowedStatuses.contains(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid booking status: " + newStatus);
        }

        // Update status
        booking.setStatus(BookingStatus.valueOf(normalizedStatus));
        bookingRepository.save(booking);

        // Update room availability based on booking status
        Room room = booking.getRoom();
        if (room != null) {
            switch (BookingStatus.valueOf(normalizedStatus)) {
                case CHECKED_IN:
                    room.setAvailable(false);
                    break;
                case CHECKED_OUT:
                case CANCELLED:
                    room.setAvailable(true);
                    break;
                default:
                    // For PENDING, CONFIRMED - keep current availability
                    break;
            }
            roomRepository.save(room);
        }
        return true;
    }

    @Override
    public void deleteBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        
        bookingRepository.delete(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingStatisticsDTO> getBookingStatisticsByMonth(String startDate) {
        if (startDate == null || startDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Start date cannot be null or empty");
        }
        return bookingRepository.getBookingStatisticsByMonth(startDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingStatisticsDTO> getBookingStatisticsByMonthAndHotel(String startDate, Long hotelId) {
        if (startDate == null || startDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Start date cannot be null or empty");
        }
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
        return bookingRepository.getBookingStatisticsByMonthAndHotel(startDate, hotelId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyRevenueStatsDTO> getMonthlyRevenueStats(Long hotelId, String startDate) {
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
        if (startDate == null || startDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Start date cannot be null or empty");
        }
        return bookingRepository.getMonthlyRevenueStats(hotelId, startDate);
    }

    @Override
    @Transactional(readOnly = true)
    public PasscodeVerificationDTO verifyBookingByPasscode(String passcode) {
        if (passcode == null || passcode.trim().isEmpty()) {
            return new PasscodeVerificationDTO(false, "Passcode cannot be empty");
        }

        // Find booking by passcode
        Optional<Booking> bookingOpt = bookingRepository.findByPasscode(passcode.trim());
        
        if (bookingOpt.isEmpty()) {
            return new PasscodeVerificationDTO(false, "Invalid passcode. No booking found.");
        }

        Booking booking = bookingOpt.get();
        
        // Check if booking is still valid (not cancelled)
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return new PasscodeVerificationDTO(false, "This booking has been cancelled.");
        }

        // Check if booking is for today or future dates
        LocalDate today = LocalDate.now();
        if (booking.getCheckInDate().isBefore(today)) {
            return new PasscodeVerificationDTO(false, "This booking is for a past date.");
        }

        // Create verification response with booking details
        PasscodeVerificationDTO verification = new PasscodeVerificationDTO(true, "Booking verified successfully!");
        verification.setBookingId(booking.getId());
        verification.setCheckInDate(booking.getCheckInDate());
        verification.setCheckOutDate(booking.getCheckOutDate());
        verification.setStatus(booking.getStatus());
        verification.setCreatedAt(booking.getCreatedAt());

        // Set guest name
        if (booking.getUser() != null) {
            verification.setGuestName(booking.getUser().getName());
        }

        // Set hotel name
        if (booking.getHotel() != null) {
            verification.setHotelName(booking.getHotel().getName());
        }

        // Set room number
        if (booking.getRoom() != null) {
            verification.setRoomNumber(booking.getRoom().getRoomNumber());
        }

        return verification;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookingsByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        List<Booking> bookings = bookingRepository.findAllBookingsByUserIdWithDetails(userId);
        return bookings.stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookingsByUserId(Long userId, Pageable pageable) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        Page<Booking> bookings = bookingRepository.findAllBookingsByUserIdWithDetails(userId, pageable);
        return bookings.map(bookingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookingsByUserIdAndStatus(Long userId, String status) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        
        List<Booking> bookings = bookingRepository.findAllBookingsByUserIdAndStatus(userId, status.trim());
        return bookings.stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Generates a unique passcode for booking.
     * Retries up to 3 times if a collision occurs.
     * 
     * @return A unique 6-character alphanumeric passcode
     * @throws RuntimeException if uniqueness cannot be guaranteed after 3 attempts
     */
    private String generateUniquePasscode() {
        final int MAX_ATTEMPTS = 3;
        
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String passcode = PasscodeGenerator.generatePasscode();
            
            if (!bookingRepository.existsByPasscode(passcode)) {
                return passcode;
            }
            
            // Log collision for debugging (optional)
            if (attempt < MAX_ATTEMPTS) {
                System.out.println("Passcode collision detected, retrying... Attempt: " + attempt);
            }
        }
        
        throw new RuntimeException("Unable to generate unique passcode after " + MAX_ATTEMPTS + " attempts");
    }

//    private void generatePasscodeAfterPayment(Long bookingId) {
//        Booking booking = bookingRepository.findByIdAndPaidTrue(bookingId)
//            .orElseThrow(() -> new RuntimeException("Booking not found or unpaid"));
//
//        String passcode = generateSecurePasscode();
//        booking.setCheckInPasscode(passcode);
//        booking.setPasscodeGeneratedAt(LocalDateTime.now());
//
//        bookingRepository.save(booking);
//
//        emailService.sendPasscodeEmail(booking.getGuestEmail(), passcode);
//    }
//
//    private String generateSecurePasscode() {
//        int length = 6;
//        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // avoid 0O1l
//        SecureRandom random = new SecureRandom();
//        StringBuilder sb = new StringBuilder();
//
//        for (int i = 0; i < length; i++) {
//            sb.append(chars.charAt(random.nextInt(chars.length())));
//        }
//
//        return sb.toString();
//    }
}