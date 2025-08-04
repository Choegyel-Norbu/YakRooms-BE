package com.yakrooms.be.util;

import java.security.SecureRandom;

/**
 * Utility class for generating cryptographically secure passcodes.
 * Generates alphanumeric codes using uppercase letters (A-Z) and numbers (0-9).
 */
public class PasscodeGenerator {
    
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int DEFAULT_LENGTH = 6;
    private static final int MAX_LENGTH = 20;
    
    /**
     * Generates a cryptographically secure alphanumeric passcode.
     * 
     * @param length The length of the passcode to generate
     * @return A unique alphanumeric passcode
     * @throws IllegalArgumentException if length is less than 1 or greater than MAX_LENGTH
     */
    public static String generatePasscode(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Passcode length must be at least 1");
        }
        
        if (length > MAX_LENGTH) {
            throw new IllegalArgumentException("Passcode length cannot exceed " + MAX_LENGTH);
        }
        
        StringBuilder passcode = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(ALPHANUMERIC_CHARS.length());
            passcode.append(ALPHANUMERIC_CHARS.charAt(randomIndex));
        }
        
        String result = passcode.toString();
        
        // Additional validation to ensure the generated passcode is valid
        if (result == null || result.isEmpty() || result.length() != length) {
            throw new IllegalStateException("Failed to generate valid passcode of length: " + length);
        }
        
        return result;
    }
    
    /**
     * Generates a 6-character alphanumeric passcode (default length).
     * 
     * @return A 6-character unique alphanumeric passcode
     */
    public static String generatePasscode() {
        return generatePasscode(DEFAULT_LENGTH);
    }
    
    /**
     * Validates if a passcode matches the expected format.
     * 
     * @param passcode The passcode to validate
     * @return true if the passcode is valid, false otherwise
     */
    public static boolean isValidPasscode(String passcode) {
        if (passcode == null || passcode.isEmpty()) {
            return false;
        }
        
        // Check if all characters are valid alphanumeric characters
        for (char c : passcode.toCharArray()) {
            if (!ALPHANUMERIC_CHARS.contains(String.valueOf(c))) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Gets the default passcode length.
     * 
     * @return The default passcode length
     */
    public static int getDefaultLength() {
        return DEFAULT_LENGTH;
    }
    
    /**
     * Gets the maximum allowed passcode length.
     * 
     * @return The maximum passcode length
     */
    public static int getMaxLength() {
        return MAX_LENGTH;
    }
} 