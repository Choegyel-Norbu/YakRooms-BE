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

import com.yakrooms.be.dto.NotificationMessage;
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
        Booking savedBooking = bookingRepository.save(booking);

        // --- START: NOTIFICATION LOGIC ---

        // Assuming your Hotel entity has a relationship to its owner (User).
        User hotelAdmin = userRepository.findByHotelId(request.getHotelId()).orElse(null);

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
        return true;
    }

    @Override
    public void deleteBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        
        bookingRepository.delete(booking);
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