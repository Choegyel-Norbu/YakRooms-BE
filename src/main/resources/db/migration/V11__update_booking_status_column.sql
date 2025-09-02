-- Ensure booking.status can store longer enum names like 'CANCELLATION_REQUESTED'
-- Switch to VARCHAR to decouple DB from Java enum list and avoid truncation

ALTER TABLE booking
    MODIFY COLUMN status VARCHAR(32) NOT NULL;

-- Optional: ensure existing values fit (no-op if already shorter)
-- Existing index on status will remain valid across type change in MySQL


