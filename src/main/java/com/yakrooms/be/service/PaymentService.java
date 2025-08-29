package com.yakrooms.be.service;

import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.enums.PaymentStatus;

/**
 * Service for managing payment status transitions and payment processing.
 * This service ensures consistent payment status management across all booking flows.
 * 
 * @author YakRooms Team
 * @version 1.0
 */
public interface PaymentService {
    
    /**
     * Process payment for a booking.
     * This method handles the actual payment processing and status updates.
     * 
     * @param booking The booking to process payment for
     * @param amount The amount to charge
     * @param paymentMethod The payment method (e.g., "CREDIT_CARD", "CASH")
     * @return true if payment was successful, false otherwise
     */
    boolean processPayment(Booking booking, java.math.BigDecimal amount, String paymentMethod);
    
    /**
     * Update payment status for a booking.
     * This method enforces valid payment status transitions.
     * 
     * @param booking The booking to update
     * @param newStatus The new payment status
     * @return true if status update was successful, false otherwise
     */
    boolean updatePaymentStatus(Booking booking, PaymentStatus newStatus);
    
    /**
     * Check if a payment status transition is valid.
     * This method enforces business rules for payment status changes.
     * 
     * @param currentStatus The current payment status
     * @param newStatus The desired new payment status
     * @return true if transition is valid, false otherwise
     */
    boolean isValidPaymentStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus);
    
    /**
     * Refund payment for a booking.
     * This method handles payment refunds and status updates.
     * 
     * @param booking The booking to refund
     * @param amount The amount to refund
     * @return true if refund was successful, false otherwise
     */
    boolean refundPayment(Booking booking, java.math.BigDecimal amount);
    
    /**
     * Get the default payment status for a new booking.
     * This method ensures consistent initial payment status across all booking types.
     * 
     * @param bookingType The type of booking (IMMEDIATE, ADVANCE, RESERVATION)
     * @return The default payment status
     */
    PaymentStatus getDefaultPaymentStatus(String bookingType);
}
