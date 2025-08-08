package com.yakrooms.be.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.yakrooms.be.model.enums.BookingStatus;
import com.yakrooms.be.model.enums.PaymentStatus;
import jakarta.validation.constraints.*;


import jakarta.persistence.*;

@Entity
@Table(name = "booking", indexes = {
    @Index(name = "idx_booking_user_id", columnList = "user_id"),
    @Index(name = "idx_booking_hotel_id", columnList = "hotel_id"),
    @Index(name = "idx_booking_room_id", columnList = "room_id"),
    @Index(name = "idx_booking_passcode", columnList = "passcode"),
    @Index(name = "idx_booking_status", columnList = "status"),
    @Index(name = "idx_booking_dates", columnList = "check_in_date, check_out_date"),
    @Index(name = "idx_booking_created_at", columnList = "created_at")
})
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "Booking.withDetails",
        attributeNodes = {
            @NamedAttributeNode("user"),
            @NamedAttributeNode("hotel"),
            @NamedAttributeNode("room")
        }
    ),
    @NamedEntityGraph(
        name = "Booking.minimal",
        attributeNodes = {
            @NamedAttributeNode("hotel"),
            @NamedAttributeNode("room")
        }
    )
})
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    @NotNull(message = "Hotel is required")
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @NotNull(message = "Room is required")
    private Room room;

    @Column(name = "phone", length = 20)
    @Pattern(regexp = "^[+]?[0-9\\-\\s()]+$", message = "Invalid phone number format")
    private String phone;

    @Column(name = "check_in_date", nullable = false)
    @NotNull(message = "Check-in date is required")
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    @Column(name = "guests", nullable = false)
    @Min(value = 1, message = "At least 1 guest is required")
    @Max(value = 20, message = "Maximum 20 guests allowed")
    private int guests;

    @Column(name = "check_in_passcode", length = 10)
    private String checkInPasscode;

    @Column(name = "passcode", unique = true, nullable = false, length = 6)
    @NotBlank(message = "Passcode is required")
    @Size(min = 6, max = 6, message = "Passcode must be exactly 6 characters")
    private String passcode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Booking status is required")
    private BookingStatus status = BookingStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.00", message = "Total price must be positive")
    private BigDecimal totalPrice;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Booking() {
        super();
    }

    public Booking(User user, Hotel hotel, Room room, LocalDate checkInDate, 
                   LocalDate checkOutDate, int guests, BigDecimal totalPrice) {
        this.user = user;
        this.hotel = hotel;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guests = guests;
        this.totalPrice = totalPrice;
        this.status = BookingStatus.PENDING;
        this.paymentStatus = PaymentStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public int getGuests() {
        return guests;
    }

    public void setGuests(int guests) {
        this.guests = guests;
    }

    public String getCheckInPasscode() {
        return checkInPasscode;
    }

    public void setCheckInPasscode(String checkInPasscode) {
        this.checkInPasscode = checkInPasscode;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business Logic Methods
    public boolean isActive() {
        return status != BookingStatus.CANCELLED && status != BookingStatus.CHECKED_OUT;
    }

    public boolean isConfirmed() {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.CHECKED_IN;
    }

    public boolean canBeModified() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

    public boolean canBeCancelled() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

    public long getStayDuration() {
        if (checkInDate != null && checkOutDate != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        }
        return 0;
    }

    public boolean isOverlapping(LocalDate checkIn, LocalDate checkOut) {
        return checkInDate.isBefore(checkOut) && checkOutDate.isAfter(checkIn);
    }

    public boolean isPastCheckIn() {
        return checkInDate != null && checkInDate.isBefore(LocalDate.now());
    }

    public boolean isPastCheckOut() {
        return checkOutDate != null && checkOutDate.isBefore(LocalDate.now());
    }

    // Validation method
    @PrePersist
    @PreUpdate
    private void validateBooking() {
        if (checkInDate != null && checkOutDate != null) {
            if (!checkOutDate.isAfter(checkInDate)) {
                throw new IllegalArgumentException("Check-out date must be after check-in date");
            }
        }
    }

    // toString, equals, and hashCode
    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", passcode='" + passcode + '\'' +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", guests=" + guests +
                ", status=" + status +
                ", totalPrice=" + totalPrice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;
        return id != null && id.equals(booking.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}