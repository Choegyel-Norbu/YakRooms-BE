-- Migration: Add index on guest_name column to booking table
-- Version: V7
-- Description: Adds index on guest_name column for better query performance when searching by guest name

CREATE INDEX idx_booking_guest_name ON booking(guest_name);
