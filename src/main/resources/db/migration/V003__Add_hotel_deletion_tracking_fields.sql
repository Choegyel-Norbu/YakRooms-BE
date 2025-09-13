-- Add hotel deletion tracking fields
-- This migration adds fields to track hotel deletion requests

ALTER TABLE hotels 
ADD COLUMN deletion_requested BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deletion_reason TEXT,
ADD COLUMN deletion_requested_at TIMESTAMP;

-- Add index for efficient querying of hotels with deletion requests
CREATE INDEX idx_hotel_deletion_requested ON hotels(deletion_requested);

-- Add composite index for admin queries
CREATE INDEX idx_hotel_deletion_requested_at ON hotels(deletion_requested, deletion_requested_at);
