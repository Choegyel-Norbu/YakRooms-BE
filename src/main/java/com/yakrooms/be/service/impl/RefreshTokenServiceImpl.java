package com.yakrooms.be.service.impl;

import com.yakrooms.be.dto.response.RefreshTokenResponse;
import com.yakrooms.be.model.entity.RefreshToken;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.repository.RefreshTokenRepository;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.security.JwtUtil;
import com.yakrooms.be.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of RefreshTokenService with security best practices
 * 
 * Security features:
 * - Token rotation on each refresh
 * - Automatic cleanup of expired tokens
 * - Rate limiting based on active token count
 * - Device and IP tracking for security monitoring
 * - Secure token hashing before storage
 */
@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Value("${jwt.refresh-token.max-per-user:5}")
    private int maxTokensPerUser;
    
    @Value("${jwt.refresh-token.cleanup-batch-size:100}")
    private int cleanupBatchSize;
    
    @Override
    public RefreshTokenResponse createRefreshToken(User user, String deviceInfo, String ipAddress) {
        // Check if user has too many active tokens
        long activeTokenCount = getActiveTokenCount(user.getId());
        if (activeTokenCount >= maxTokensPerUser) {
            // Revoke oldest tokens to make room
            revokeOldestTokens(user.getId(), (int) (activeTokenCount - maxTokensPerUser + 1));
        }
        
        // Generate new refresh token
        String token = jwtUtil.generateRefreshToken(user);
        String tokenHash = jwtUtil.generateTokenHash(token);
        
        // Calculate expiration time (7 days from now)
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
        
        // Create refresh token entity
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(tokenHash); // Store only the hash, never the actual token
        refreshToken.setUserId(user.getId());
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setDeviceInfo(deviceInfo);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setIsRevoked(false);
        
        // Save to database
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        
        // Return response with actual JWT token (not stored in DB)
        return new RefreshTokenResponse(
            token, // Actual JWT token for client
            savedToken.getUserId(),
            savedToken.getExpiresAt(),
            savedToken.getDeviceInfo(),
            savedToken.getIpAddress(),
            savedToken.getCreatedAt()
        );
    }
    
    @Override
    public RefreshTokenResponse validateAndRotateToken(String token, String deviceInfo, String ipAddress) {
        // Validate JWT token
        if (!jwtUtil.validateRefreshToken(token)) {
            throw new SecurityException("Invalid refresh token");
        }
        
        // Extract user information
        Long userId = jwtUtil.extractUserId(token);
        String tokenHash = jwtUtil.generateTokenHash(token);
        
        // Find token in database
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHashAndNotRevoked(tokenHash);
        if (tokenOpt.isEmpty()) {
            throw new SecurityException("Refresh token not found or revoked");
        }
        
        RefreshToken existingToken = tokenOpt.get();
        
        // Check if token is expired
        if (existingToken.isExpired()) {
            // Clean up expired token
            refreshTokenRepository.delete(existingToken);
            throw new SecurityException("Refresh token expired");
        }
        
        // Get user from database
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new SecurityException("User not found");
        }
        
        User user = userOpt.get();
        
        // Revoke the old token
        existingToken.revoke();
        refreshTokenRepository.save(existingToken);
        
        // Create new refresh token (token rotation)
        RefreshTokenResponse newToken = createRefreshToken(user, deviceInfo, ipAddress);
        
        return newToken;
    }
    
    @Override
    public int revokeAllUserTokens(Long userId) {
        return refreshTokenRepository.revokeAllTokensByUserId(userId, LocalDateTime.now());
    }
    
    @Override
    public boolean revokeToken(String tokenHash) {
        int updated = refreshTokenRepository.revokeTokenByHash(tokenHash, LocalDateTime.now());
        return updated > 0;
    }
    
    @Override
    public int cleanupExpiredTokens() {
        return refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getActiveTokenCount(Long userId) {
        return refreshTokenRepository.countActiveTokensByUserId(userId, LocalDateTime.now());
    }
    
    /**
     * Revoke oldest tokens for a user to make room for new ones
     */
    private void revokeOldestTokens(Long userId, int count) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findValidTokensByUserId(userId, LocalDateTime.now());
        
        // Sort by creation time and revoke oldest
        activeTokens.stream()
                .sorted((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                .limit(count)
                .forEach(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                });
    }
    
    /**
     * Get refresh token by hash (for internal use)
     */
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return refreshTokenRepository.findByTokenHashAndNotRevoked(tokenHash);
    }
    
    /**
     * Get all active tokens for a user (for security monitoring)
     */
    public List<RefreshToken> getActiveTokensForUser(Long userId) {
        return refreshTokenRepository.findValidTokensByUserId(userId, LocalDateTime.now());
    }
}
