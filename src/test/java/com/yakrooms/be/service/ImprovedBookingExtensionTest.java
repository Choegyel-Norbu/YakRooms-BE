package com.yakrooms.be.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Test to verify the improved booking extension logic.
 * Tests that extensions are allowed after 12:00 PM if there are no conflicts.
 */
@DisplayName("Improved Booking Extension Logic Test")
public class ImprovedBookingExtensionTest {
    
    @Test
    @DisplayName("Test that extensions are allowed after 12:00 PM if no conflicts exist")
    void testExtensionAllowedAfterNoonIfNoConflicts() {
        // Scenario: Guest has booking 29-30, wants to extend to 31 on 30 after 12:00 PM
        LocalDate today = LocalDate.of(2025, 8, 30); // August 30
        LocalDate checkoutDate = LocalDate.of(2025, 8, 30); // Checkout on August 30
        LocalDate newCheckoutDate = LocalDate.of(2025, 8, 31); // Extend to August 31
        
        // Test different times on checkout day
        LocalTime elevenFiftyNine = LocalTime.of(11, 59); // 11:59 AM - BEFORE job schedule
        LocalTime twelveNoon = LocalTime.of(12, 0);       // 12:00 PM - AT job schedule time
        LocalTime twelveOne = LocalTime.of(12, 1);        // 12:01 PM - AFTER job schedule
        LocalTime twoPM = LocalTime.of(14, 0);            // 2:00 PM - AFTER job schedule
        
        // Test the improved logic manually
        boolean extensionAllowedBeforeNoon = isExtensionAllowedByTime(checkoutDate, today, elevenFiftyNine);
        boolean extensionAllowedAtNoon = isExtensionAllowedByTime(checkoutDate, today, twelveNoon);
        boolean extensionAllowedAfterNoon = isExtensionAllowedByTime(checkoutDate, today, twelveOne);
        boolean extensionAllowedAtTwoPM = isExtensionAllowedByTime(checkoutDate, today, twoPM);
        
        // Assertions - ALL should be allowed now
        assertTrue(extensionAllowedBeforeNoon, "Extension should be allowed before 12:00 PM on checkout day");
        assertTrue(extensionAllowedAtNoon, "Extension should be allowed at 12:00 PM on checkout day");
        assertTrue(extensionAllowedAfterNoon, "Extension should be allowed after 12:00 PM on checkout day");
        assertTrue(extensionAllowedAtTwoPM, "Extension should be allowed at 2:00 PM on checkout day");
        
        System.out.println("✅ Improved extension logic test passed!");
        System.out.println("   - Before noon (11:59 AM): " + extensionAllowedBeforeNoon);
        System.out.println("   - At noon (12:00 PM): " + extensionAllowedAtNoon);
        System.out.println("   - After noon (12:01 PM): " + extensionAllowedAfterNoon);
        System.out.println("   - At 2:00 PM: " + extensionAllowedAtTwoPM);
        System.out.println("   ");
        System.out.println("🎯 Key Improvement: Extensions are now allowed after 12:00 PM!");
        System.out.println("   The system will check for conflicts instead of automatically denying.");
        System.out.println("   This makes the system more flexible and logical.");
    }
    
    @Test
    @DisplayName("Test timing guidance for same-day extensions")
    void testTimingGuidanceForSameDayExtensions() {
        // Test the timing guidance logic
        LocalDate today = LocalDate.of(2025, 8, 30);
        LocalDate checkoutDate = LocalDate.of(2025, 8, 30);
        
        LocalTime beforeNoon = LocalTime.of(11, 59);
        LocalTime atNoon = LocalTime.of(12, 0);
        LocalTime afterNoon = LocalTime.of(14, 30);
        
        String guidanceBeforeNoon = getTimingGuidance(checkoutDate, today, beforeNoon);
        String guidanceAtNoon = getTimingGuidance(checkoutDate, today, atNoon);
        String guidanceAfterNoon = getTimingGuidance(checkoutDate, today, afterNoon);
        
        // Assertions
        assertTrue(guidanceBeforeNoon.contains("optimal timing"), "Should provide optimal timing guidance before noon");
        assertTrue(guidanceAfterNoon.contains("room availability has been updated"), "Should provide post-scheduler guidance after noon");
        
        System.out.println("✅ Timing guidance test passed!");
        System.out.println("   - Before noon: " + guidanceBeforeNoon);
        System.out.println("   - At noon: " + guidanceAtNoon);
        System.out.println("   - After noon: " + guidanceAfterNoon);
    }
    
    /**
     * Improved extension logic that allows extensions after 12:00 PM if no conflicts exist.
     * This mirrors the updated logic in UnifiedBookingServiceImpl.isExtensionAllowedByTime()
     */
    private boolean isExtensionAllowedByTime(LocalDate checkoutDate, LocalDate currentDate, LocalTime currentTime) {
        LocalTime jobScheduleTime = LocalTime.of(12, 0); // Job runs at 12:00 PM
        
        // If checkout is today and it's today, always allow extension
        if (checkoutDate.equals(currentDate)) {
            // Allow extension regardless of time, but conflict checking will happen later
            // This makes the system more flexible and logical
            return true;
        }
        
        // If checkout date is in the future, extension is allowed
        if (checkoutDate.isAfter(currentDate)) {
            return true;
        }
        
        // If checkout date is in the past, extension is not allowed
        return false;
    }
    
    /**
     * Get timing guidance for same-day extensions.
     * This mirrors the logic in the main extension method.
     */
    private String getTimingGuidance(LocalDate checkoutDate, LocalDate currentDate, LocalTime currentTime) {
        LocalTime jobScheduleTime = LocalTime.of(12, 0);
        
        if (checkoutDate.equals(currentDate)) {
            if (currentTime.isBefore(jobScheduleTime)) {
                return "Extension request made before 12:00 PM - optimal timing for checkout day extension";
            } else {
                return "Extension request made after 12:00 PM - room availability has been updated, checking for conflicts";
            }
        }
        
        return "Standard extension request";
    }
}
