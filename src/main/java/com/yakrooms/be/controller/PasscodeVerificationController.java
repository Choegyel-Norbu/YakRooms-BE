package com.yakrooms.be.controller;

import com.yakrooms.be.dto.PasscodeVerificationDTO;
import com.yakrooms.be.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for passcode verification operations.
 * Handles booking verification by passcode.
 */
@RestController
@RequestMapping("/api/passcode")
public class PasscodeVerificationController {

    @Autowired
    private BookingService bookingService;

    /**
     * Verify booking by passcode.
     * Only HOTEL_ADMIN and STAFF can verify passcodes.
     * 
     * @param passcode The passcode to verify
     * @return PasscodeVerificationDTO with verification result and booking details
     */
    @PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
    @PostMapping("/verify")
    public ResponseEntity<PasscodeVerificationDTO> verifyBookingByPasscode(@RequestParam String passcode) {
        try {
            PasscodeVerificationDTO verification = bookingService.verifyBookingByPasscode(passcode);
            return ResponseEntity.ok(verification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new PasscodeVerificationDTO(false, "Error during verification: " + e.getMessage()));
        }
    }

    /**
     * Verify booking by passcode using GET method (for easier testing).
     * Only HOTEL_ADMIN and STAFF can verify passcodes.
     * 
     * @param passcode The passcode to verify
     * @return PasscodeVerificationDTO with verification result and booking details
     */
    @PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
    @GetMapping("/verify")
    public ResponseEntity<PasscodeVerificationDTO> verifyBookingByPasscodeGet(@RequestParam String passcode) {
        try {
            PasscodeVerificationDTO verification = bookingService.verifyBookingByPasscode(passcode);
            return ResponseEntity.ok(verification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new PasscodeVerificationDTO(false, "Error during verification: " + e.getMessage()));
        }
    }
} 