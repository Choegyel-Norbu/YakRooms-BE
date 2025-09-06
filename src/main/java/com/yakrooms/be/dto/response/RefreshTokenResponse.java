package com.yakrooms.be.dto.response;

import java.time.LocalDateTime;

/**
 * Response DTO for refresh token operations
 * Contains the actual JWT token for client use and metadata
 */
public class RefreshTokenResponse {
    
    private String token; // The actual JWT refresh token
    private Long userId;
    private LocalDateTime expiresAt;
    private String deviceInfo;
    private String ipAddress;
    private LocalDateTime createdAt;
    
    // Constructors
    public RefreshTokenResponse() {}
    
    public RefreshTokenResponse(String token, Long userId, LocalDateTime expiresAt, 
                               String deviceInfo, String ipAddress, LocalDateTime createdAt) {
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
