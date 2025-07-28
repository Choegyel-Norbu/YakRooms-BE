-- Add unique index on passcode column for booking table
-- This ensures database-level uniqueness constraint
CREATE UNIQUE INDEX idx_booking_passcode_unique ON booking(passcode); 