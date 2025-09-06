package com.yakrooms.be.service;

import com.yakrooms.be.dto.response.RefreshTokenResponse;
import com.yakrooms.be.model.entity.RefreshToken;
import com.yakrooms.be.model.entity.User;

/**
 * Service interface for refresh token management
 */
public interface RefreshTokenService {
    
    /**
     * Create a new refresh token for a user
     * @param user The user to create token for
     * @param deviceInfo Device information for security tracking
     * @param ipAddress IP address for security tracking
     * @return The refresh token response with actual JWT token
     */
    RefreshTokenResponse createRefreshToken(User user, String deviceInfo, String ipAddress);
    
    /**
     * Validate and rotate refresh token
     * @param token The refresh token to validate
     * @param deviceInfo Current device information
     * @param ipAddress Current IP address
     * @return New refresh token response if validation succeeds
     * @throws SecurityException if token is invalid or expired
     */
    RefreshTokenResponse validateAndRotateToken(String token, String deviceInfo, String ipAddress);
    
    /**
     * Revoke all refresh tokens for a user
     * @param userId The user ID
     * @return Number of tokens revoked
     */
    int revokeAllUserTokens(Long userId);
    
    /**
     * Revoke a specific refresh token
     * @param tokenHash The token hash to revoke
     * @return true if token was found and revoked
     */
    boolean revokeToken(String tokenHash);
    
    /**
     * Clean up expired tokens
     * @return Number of tokens cleaned up
     */
    int cleanupExpiredTokens();
    
    /**
     * Get active token count for a user
     * @param userId The user ID
     * @return Number of active tokens
     */
    long getActiveTokenCount(Long userId);
}
