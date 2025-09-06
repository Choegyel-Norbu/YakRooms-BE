-- Migration to create refresh_tokens table with proper schema
-- This ensures the token_hash column can store SHA-256 hashes (64 characters)

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE COMMENT 'SHA-256 hash of the refresh token (64 hex characters)',
    user_id BIGINT NOT NULL COMMENT 'Foreign key to users table',
    expires_at TIMESTAMP NOT NULL COMMENT 'Token expiration timestamp',
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether the token has been revoked',
    revoked_at TIMESTAMP NULL COMMENT 'When the token was revoked',
    device_info VARCHAR(500) NULL COMMENT 'Device information for security tracking',
    ip_address VARCHAR(45) NULL COMMENT 'IP address (supports both IPv4 and IPv6)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When the record was created',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'When the record was last updated',
    
    -- Indexes for performance
    INDEX idx_refresh_token_hash (token_hash),
    INDEX idx_refresh_token_user (user_id),
    INDEX idx_refresh_token_expires (expires_at),
    INDEX idx_refresh_token_active (user_id, is_revoked, expires_at)
) ENGINE=InnoDB 
  CHARACTER SET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='Secure refresh token storage with hashed tokens';

-- Add constraint to ensure expires_at is in the future for new tokens
-- ALTER TABLE refresh_tokens ADD CONSTRAINT chk_expires_future 
--   CHECK (expires_at > created_at);
