package com.yakrooms.be.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yakrooms.be.exception.BusinessException;
import com.yakrooms.be.model.entity.Booking;
import com.yakrooms.be.model.enums.PaymentStatus;
import com.yakrooms.be.service.PaymentService;

/**
 * Implementation of PaymentService with proper status transition validation.
 * This service enforces business rules for payment status changes and ensures
 * consistency across all booking flows.
 * 
 * @author YakRooms Team
 * @version 1.0
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
    
    // Define valid payment status transitions
    private static final Map<PaymentStatus, Set<PaymentStatus>> VALID_TRANSITIONS = new HashMap<>();
    
    static {
        // PENDING can transition to PAID, FAILED, or CANCELLED
        VALID_TRANSITIONS.put(PaymentStatus.PENDING, 
            Set.of(PaymentStatus.PAID, PaymentStatus.FAILED, PaymentStatus.CANCELLED));
        
        // PAID can transition to REFUNDED or CANCELLED
        VALID_TRANSITIONS.put(PaymentStatus.PAID, 
            Set.of(PaymentStatus.REFUNDED, PaymentStatus.CANCELLED));
        
        // FAILED can transition to PENDING (retry) or CANCELLED
        VALID_TRANSITIONS.put(PaymentStatus.FAILED, 
            Set.of(PaymentStatus.PENDING, PaymentStatus.CANCELLED));
        
        // REFUNDED is a terminal state
        VALID_TRANSITIONS.put(PaymentStatus.REFUNDED, Set.of());
        
        // CANCELLED is a terminal state
        VALID_TRANSITIONS.put(PaymentStatus.CANCELLED, Set.of());
        
        // EXPIRED can transition to PENDING (renewal) or CANCELLED
        VALID_TRANSITIONS.put(PaymentStatus.EXPIRED, 
            Set.of(PaymentStatus.PENDING, PaymentStatus.CANCELLED));
    }
    
    @Override
    @Transactional
    public boolean processPayment(Booking booking, BigDecimal amount, String paymentMethod) {
        logger.info("Processing payment for booking {}: amount={}, method={}", 
                   booking.getId(), amount, paymentMethod);
        
        try {
            // Validate payment amount
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                logger.error("Invalid payment amount: {}", amount);
                throw new BusinessException("Invalid payment amount");
            }
            
            // Check if amount matches booking total
            if (amount.compareTo(booking.getTotalPrice()) != 0) {
                logger.error("Payment amount {} does not match booking total {}", 
                           amount, booking.getTotalPrice());
                throw new BusinessException("Payment amount does not match booking total");
            }
            
            // Simulate payment processing (replace with actual payment gateway integration)
            boolean paymentSuccess = simulatePaymentProcessing(amount, paymentMethod);
            
            if (paymentSuccess) {
                // Update payment status to PAID
                updatePaymentStatus(booking, PaymentStatus.PAID);
                logger.info("Payment processed successfully for booking {}", booking.getId());
                return true;
            } else {
                // Update payment status to FAILED
                updatePaymentStatus(booking, PaymentStatus.FAILED);
                logger.warn("Payment processing failed for booking {}", booking.getId());
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error processing payment for booking {}: {}", booking.getId(), e.getMessage());
            // Update payment status to FAILED
            updatePaymentStatus(booking, PaymentStatus.FAILED);
            throw new BusinessException("Payment processing failed: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updatePaymentStatus(Booking booking, PaymentStatus newStatus) {
        logger.debug("Updating payment status for booking {}: {} -> {}", 
                    booking.getId(), booking.getPaymentStatus(), newStatus);
        
        PaymentStatus currentStatus = booking.getPaymentStatus();
        
        // Validate the transition
        if (!isValidPaymentStatusTransition(currentStatus, newStatus)) {
            logger.error("Invalid payment status transition: {} -> {} for booking {}", 
                        currentStatus, newStatus, booking.getId());
            throw new BusinessException(String.format("Invalid payment status transition from %s to %s", 
                                                    currentStatus, newStatus));
        }
        
        // Update the status
        booking.setPaymentStatus(newStatus);
        
        logger.info("Successfully updated payment status for booking {}: {} -> {}", 
                   booking.getId(), currentStatus, newStatus);
        
        return true;
    }
    
    @Override
    public boolean isValidPaymentStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }
        
        // Same status is always valid
        if (currentStatus == newStatus) {
            return true;
        }
        
        // Check if transition is allowed
        Set<PaymentStatus> allowedTransitions = VALID_TRANSITIONS.get(currentStatus);
        if (allowedTransitions == null) {
            logger.warn("No transition rules defined for payment status: {}", currentStatus);
            return false;
        }
        
        boolean isValid = allowedTransitions.contains(newStatus);
        
        if (!isValid) {
            logger.debug("Invalid payment status transition: {} -> {} (allowed: {})", 
                        currentStatus, newStatus, allowedTransitions);
        }
        
        return isValid;
    }
    
    @Override
    @Transactional
    public boolean refundPayment(Booking booking, BigDecimal amount) {
        logger.info("Processing refund for booking {}: amount={}", booking.getId(), amount);
        
        try {
            // Validate refund amount
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                logger.error("Invalid refund amount: {}", amount);
                throw new BusinessException("Invalid refund amount");
            }
            
            // Check if refund amount is valid
            if (amount.compareTo(booking.getTotalPrice()) > 0) {
                logger.error("Refund amount {} exceeds booking total {}", 
                           amount, booking.getTotalPrice());
                throw new BusinessException("Refund amount cannot exceed booking total");
            }
            
            // Check if booking is in a refundable state
            if (booking.getPaymentStatus() != PaymentStatus.PAID) {
                logger.error("Cannot refund booking {} with payment status: {}", 
                           booking.getId(), booking.getPaymentStatus());
                throw new BusinessException("Only paid bookings can be refunded");
            }
            
            // Simulate refund processing (replace with actual refund logic)
            boolean refundSuccess = simulateRefundProcessing(amount);
            
            if (refundSuccess) {
                // Update payment status to REFUNDED
                updatePaymentStatus(booking, PaymentStatus.REFUNDED);
                logger.info("Refund processed successfully for booking {}", booking.getId());
                return true;
            } else {
                logger.warn("Refund processing failed for booking {}", booking.getId());
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error processing refund for booking {}: {}", booking.getId(), e.getMessage());
            throw new BusinessException("Refund processing failed: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentStatus getDefaultPaymentStatus(String bookingType) {
        if (bookingType == null) {
            return PaymentStatus.PENDING;
        }
        
        // Define default payment status based on booking type
        switch (bookingType.toUpperCase()) {
            case "IMMEDIATE":
                // Immediate bookings start as PENDING (payment required at check-in)
                return PaymentStatus.PENDING;
                
            case "ADVANCE":
                // Advance bookings start as PENDING (payment required before check-in)
                return PaymentStatus.PENDING;
                
            case "RESERVATION":
                // Reservations start as PENDING (payment required to confirm)
                return PaymentStatus.PENDING;
                
            default:
                logger.warn("Unknown booking type: {}, defaulting to PENDING", bookingType);
                return PaymentStatus.PENDING;
        }
    }
    
    /**
     * Simulate payment processing (replace with actual payment gateway integration).
     * 
     * @param amount The amount to charge
     * @param paymentMethod The payment method
     * @return true if payment was successful, false otherwise
     */
    private boolean simulatePaymentProcessing(BigDecimal amount, String paymentMethod) {
        // Simulate 95% success rate for demonstration
        // In production, integrate with actual payment gateway (Stripe, PayPal, etc.)
        double successRate = 0.95;
        return Math.random() < successRate;
    }
    
    /**
     * Simulate refund processing (replace with actual refund logic).
     * 
     * @param amount The amount to refund
     * @return true if refund was successful, false otherwise
     */
    private boolean simulateRefundProcessing(BigDecimal amount) {
        // Simulate 98% success rate for demonstration
        // In production, integrate with actual refund processing
        double successRate = 0.98;
        return Math.random() < successRate;
    }
}
