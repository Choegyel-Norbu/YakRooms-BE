-- Emergency fix for token_hash column size issue
-- This migration ensures the column can store SHA-256 hashes properly

-- Check if the column exists and modify it to ensure proper size
ALTER TABLE refresh_tokens 
MODIFY COLUMN token_hash VARCHAR(64) NOT NULL UNIQUE 
COMMENT 'SHA-256 hash of the refresh token (exactly 64 hex characters)';

-- Verify the column definition is correct
-- SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE, COLUMN_DEFAULT
-- FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'refresh_tokens' AND COLUMN_NAME = 'token_hash';
