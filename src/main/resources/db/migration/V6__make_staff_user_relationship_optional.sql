-- Make staff user_id foreign key nullable to allow optional relationship
ALTER TABLE staff MODIFY COLUMN user_id BIGINT NULL; 