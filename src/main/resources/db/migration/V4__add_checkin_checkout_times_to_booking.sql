-- Migration V4: Add check-in and check-out time columns to booking table
-- This migration adds time-based precision to room availability management

-- Add check-in time column with default value of 00:00:00 (12:00 AM midnight)
ALTER TABLE booking 
ADD COLUMN check_in_time TIME NOT NULL DEFAULT '00:00:00' COMMENT 'Check-in time (default: 12:00 AM midnight)';

-- Add check-out time column with default value of 12:00:00 (12:00 PM noon)
ALTER TABLE booking 
ADD COLUMN check_out_time TIME NOT NULL DEFAULT '12:00:00' COMMENT 'Check-out time (default: 12:00 PM noon)';

-- Add index for time-based queries to improve performance
CREATE INDEX idx_booking_checkin_time ON booking(check_in_time);
CREATE INDEX idx_booking_checkout_time ON booking(check_out_time);

-- Add composite index for date and time queries
CREATE INDEX idx_booking_dates_times ON booking(check_in_date, check_in_time, check_out_date, check_out_time);

-- Update existing bookings to have the default times
-- This ensures all existing bookings have consistent time values
UPDATE booking 
SET check_in_time = '00:00:00', check_out_time = '12:00:00' 
WHERE check_in_time IS NULL OR check_out_time IS NULL;
