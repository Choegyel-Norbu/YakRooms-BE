-- Remove room_id column from notifications table
-- Room information is accessible through the booking relationship (notification.booking.room)
-- This eliminates redundant data and follows the principle of single source of truth

-- First, drop any existing foreign key constraint on room_id
SET @constraint_name = (
    SELECT CONSTRAINT_NAME 
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'notifications' 
    AND COLUMN_NAME = 'room_id' 
    AND CONSTRAINT_NAME != 'PRIMARY'
    AND CONSTRAINT_NAME LIKE '%room_id%'
    LIMIT 1
);

-- Drop the foreign key constraint if it exists
SET @sql = IF(@constraint_name IS NOT NULL, 
    CONCAT('ALTER TABLE notifications DROP FOREIGN KEY ', @constraint_name), 
    'SELECT "No foreign key constraint found on room_id" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop any index on room_id
DROP INDEX IF EXISTS idx_notifications_room_id ON notifications;

-- Remove the room_id column
ALTER TABLE notifications DROP COLUMN IF EXISTS room_id;
