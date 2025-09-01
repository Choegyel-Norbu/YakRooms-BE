-- Migration: Add guest_name column to booking table
-- Version: V3
-- Description: Adds guest_name column to store the name of the guest for each booking

ALTER TABLE booking ADD COLUMN guest_name VARCHAR(255);



