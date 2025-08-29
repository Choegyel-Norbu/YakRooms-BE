-- Version: V5
-- Description: Remove version column from room table (no longer using optimistic locking)

ALTER TABLE room DROP COLUMN version;
