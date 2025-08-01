-- Add CASCADE DELETE constraint to room table's hotel_id foreign key
-- Drop existing foreign key constraint (adjust constraint name if needed)
ALTER TABLE room DROP FOREIGN KEY IF EXISTS room_ibfk_1;

-- Add new foreign key constraint with CASCADE DELETE
ALTER TABLE room 
ADD CONSTRAINT FK_room_hotel_id 
FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE; 