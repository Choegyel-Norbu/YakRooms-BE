-- Add CASCADE DELETE constraints to hotel collections
-- Drop existing foreign key constraints (adjust constraint names if needed)
ALTER TABLE hotel_amenities DROP FOREIGN KEY IF EXISTS hotel_amenities_ibfk_1;
ALTER TABLE hotel_photo_urls DROP FOREIGN KEY IF EXISTS hotel_photo_urls_ibfk_1;

-- Add new foreign key constraints with CASCADE DELETE
ALTER TABLE hotel_amenities 
ADD CONSTRAINT FK_hotel_amenities_hotel_id 
FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE;

ALTER TABLE hotel_photo_urls 
ADD CONSTRAINT FK_hotel_photo_urls_hotel_id 
FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE; 