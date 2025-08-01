-- Add CASCADE DELETE constraints to room collections
-- Drop existing foreign key constraints
ALTER TABLE room_amenities DROP FOREIGN KEY FK5hjyttdqm1931pct5kaix8d6x;
ALTER TABLE room_image_urls DROP FOREIGN KEY IF EXISTS room_image_urls_ibfk_1;

-- Add new foreign key constraints with CASCADE DELETE
ALTER TABLE room_amenities 
ADD CONSTRAINT FK_room_amenities_room_id 
FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE;

ALTER TABLE room_image_urls 
ADD CONSTRAINT FK_room_image_urls_room_id 
FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE; 