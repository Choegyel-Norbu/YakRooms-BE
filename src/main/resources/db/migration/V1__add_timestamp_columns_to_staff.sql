-- Add timestamp columns to staff table
ALTER TABLE staff 
ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at DATETIME NULL;

-- Add indexes for the new columns
CREATE INDEX idx_staff_created_at ON staff(created_at);
CREATE INDEX idx_staff_updated_at ON staff(updated_at); 