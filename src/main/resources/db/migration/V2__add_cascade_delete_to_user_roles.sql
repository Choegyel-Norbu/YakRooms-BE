-- Add CASCADE DELETE constraint to user_roles table
-- This ensures that when a user is deleted, all related UserRole records are also deleted

ALTER TABLE user_roles 
DROP FOREIGN KEY IF EXISTS fk_user_roles_user_id;

ALTER TABLE user_roles 
ADD CONSTRAINT fk_user_roles_user_id 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE CASCADE; 