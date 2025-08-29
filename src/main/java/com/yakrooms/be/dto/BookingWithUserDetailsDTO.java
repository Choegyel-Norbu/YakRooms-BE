package com.yakrooms.be.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import com.yakrooms.be.model.enums.BookingStatus;
import com.yakrooms.be.model.enums.Role;

/**
 * DTO representing booking details with complete user information.
 * Used for comprehensive booking views and reporting.
 */
public class BookingWithUserDetailsDTO {
    
    // Booking Information
    private Long bookingId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private String passcode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String guestName;
    private String guestPhone;
    private String guestEmail;
    private String specialRequests;
    private Long roomId;
    private Long hotelId;
    
    // User Information
    private Long userId;
    private String userEmail;
    private String userName;
    private String userPhone;
    private String userProfilePicUrl;
    private Set<Role> userRoles;
    private Boolean userIsActive;
    private LocalDateTime userLastLogin;
    private LocalDateTime userCreatedAt;
    private LocalDateTime userUpdatedAt;
    
    // Default constructor
    public BookingWithUserDetailsDTO() {}
    
    // Constructor with all fields
    public BookingWithUserDetailsDTO(Long bookingId, LocalDate checkInDate, LocalDate checkOutDate, 
                                   BigDecimal totalPrice, BookingStatus status, String passcode,
                                   LocalDateTime createdAt, LocalDateTime updatedAt, String guestName,
                                   String guestPhone, String guestEmail, String specialRequests,
                                   Long roomId, Long hotelId, Long userId, String userEmail,
                                   String userName, String userPhone, String userProfilePicUrl,
                                   Set<Role> userRoles, Boolean userIsActive, LocalDateTime userLastLogin,
                                   LocalDateTime userCreatedAt, LocalDateTime userUpdatedAt) {
        this.bookingId = bookingId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.passcode = passcode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.guestName = guestName;
        this.guestPhone = guestPhone;
        this.guestEmail = guestEmail;
        this.specialRequests = specialRequests;
        this.roomId = roomId;
        this.hotelId = hotelId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userProfilePicUrl = userProfilePicUrl;
        this.userRoles = userRoles;
        this.userIsActive = userIsActive;
        this.userLastLogin = userLastLogin;
        this.userCreatedAt = userCreatedAt;
        this.userUpdatedAt = userUpdatedAt;
    }
    
    // Getters and Setters
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    
    public String getPasscode() { return passcode; }
    public void setPasscode(String passcode) { this.passcode = passcode; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    
    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }
    
    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
    
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    
    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    
    public String getUserProfilePicUrl() { return userProfilePicUrl; }
    public void setUserProfilePicUrl(String userProfilePicUrl) { this.userProfilePicUrl = userProfilePicUrl; }
    
    public Set<Role> getUserRoles() { return userRoles; }
    public void setUserRoles(Set<Role> userRoles) { this.userRoles = userRoles; }
    
    public Boolean getUserIsActive() { return userIsActive; }
    public void setUserIsActive(Boolean userIsActive) { this.userIsActive = userIsActive; }
    
    public LocalDateTime getUserLastLogin() { return userLastLogin; }
    public void setUserLastLogin(LocalDateTime userLastLogin) { this.userLastLogin = userLastLogin; }
    
    public LocalDateTime getUserCreatedAt() { return userCreatedAt; }
    public void setUserCreatedAt(LocalDateTime userCreatedAt) { this.userCreatedAt = userCreatedAt; }
    
    public LocalDateTime getUserUpdatedAt() { return userUpdatedAt; }
    public void setUserUpdatedAt(LocalDateTime userUpdatedAt) { this.userUpdatedAt = userUpdatedAt; }
}
