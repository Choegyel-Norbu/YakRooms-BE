package com.yakrooms.be.repository;

import com.yakrooms.be.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RefreshToken entity with optimized queries
 * 
 * Security considerations:
 * - Automatic cleanup of expired tokens
 * - Batch operations for performance
 * - Indexed queries for fast lookups
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    /**
     * Find refresh token by hash
     * Used for token validation during refresh
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.tokenHash = :tokenHash AND rt.isRevoked = false")
    Optional<RefreshToken> findByTokenHashAndNotRevoked(@Param("tokenHash") String tokenHash);
    
    /**
     * Find all valid refresh tokens for a user
     * Used for token management and security monitoring
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.userId = :userId AND rt.isRevoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    /**
     * Revoke all refresh tokens for a user
     * Used during logout or security incidents
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true, rt.revokedAt = :revokedAt WHERE rt.userId = :userId AND rt.isRevoked = false")
    int revokeAllTokensByUserId(@Param("userId") Long userId, @Param("revokedAt") LocalDateTime revokedAt);
    
    /**
     * Revoke a specific refresh token
     * Used for single token logout
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true, rt.revokedAt = :revokedAt WHERE rt.tokenHash = :tokenHash")
    int revokeTokenByHash(@Param("tokenHash") String tokenHash, @Param("revokedAt") LocalDateTime revokedAt);
    
    /**
     * Clean up expired tokens
     * Should be called periodically for maintenance
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * Count active tokens for a user
     * Used for security monitoring and rate limiting
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.userId = :userId AND rt.isRevoked = false AND rt.expiresAt > :now")
    long countActiveTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    /**
     * Find tokens by user and device info
     * Used for device-specific token management
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.userId = :userId AND rt.deviceInfo = :deviceInfo AND rt.isRevoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findActiveTokensByUserAndDevice(@Param("userId") Long userId, 
                                                      @Param("deviceInfo") String deviceInfo, 
                                                      @Param("now") LocalDateTime now);
}
