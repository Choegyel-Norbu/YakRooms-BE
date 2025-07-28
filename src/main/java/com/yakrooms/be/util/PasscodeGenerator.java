package com.yakrooms.be.util;

import java.security.SecureRandom;

/**
 * Utility class for generating cryptographically secure passcodes.
 * Generates alphanumeric codes using uppercase letters (A-Z) and numbers (0-9).
 */
public class PasscodeGenerator {
    
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    /**
     * Generates a cryptographically secure alphanumeric passcode.
     * 
     * @param length The length of the passcode to generate
     * @return A unique alphanumeric passcode
     * @throws IllegalArgumentException if length is less than 1
     */
    public static String generatePasscode(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Passcode length must be at least 1");
        }
        
        StringBuilder passcode = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(ALPHANUMERIC_CHARS.length());
            passcode.append(ALPHANUMERIC_CHARS.charAt(randomIndex));
        }
        
        return passcode.toString();
    }
    
    /**
     * Generates a 6-character alphanumeric passcode (default length).
     * 
     * @return A 6-character unique alphanumeric passcode
     */
    public static String generatePasscode() {
        return generatePasscode(6);
    }
} 