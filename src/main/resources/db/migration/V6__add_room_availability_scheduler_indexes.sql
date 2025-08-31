-- Add indexes for room availability scheduler queries
-- These indexes optimize the daily noon scheduler that updates room availability

-- Index for finding rooms that should become available (checkout completed today)
-- Covers: check_out_date + status for CONFIRMED and CHECKED_IN bookings
CREATE INDEX idx_booking_checkout_status ON booking (check_out_date, status);

-- Index for finding rooms that should become unavailable (checkin starting today)  
-- Covers: check_in_date + status for CONFIRMED and PENDING bookings
CREATE INDEX idx_booking_checkin_status ON booking (check_in_date, status);

-- Composite index for room availability queries with room_id
-- This helps when filtering by specific rooms
CREATE INDEX idx_booking_room_checkout_status ON booking (room_id, check_out_date, status);
CREATE INDEX idx_booking_room_checkin_status ON booking (room_id, check_in_date, status);

-- Index for bulk room availability updates
-- This optimizes the bulk update operations in RoomRepository
CREATE INDEX idx_room_hotel_available ON room (hotel_id, is_available);
