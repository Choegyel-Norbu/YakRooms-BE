-- Add booking_id column to notifications table
ALTER TABLE notifications ADD COLUMN booking_id BIGINT;

-- Add foreign key constraint
ALTER TABLE notifications ADD CONSTRAINT fk_notifications_booking_id 
    FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE;

-- Add index for better query performance
CREATE INDEX idx_notifications_booking_id ON notifications(booking_id);

-- Add index for notification type for better filtering
CREATE INDEX idx_notifications_type ON notifications(type);

-- Add index for is_read status for better filtering
CREATE INDEX idx_notifications_is_read ON notifications(is_read);

-- Add index for created_at for better ordering
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
