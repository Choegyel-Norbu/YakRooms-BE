-- Add CASCADE DELETE constraint to user_roles table's user_id foreign key
-- Drop existing foreign key constraint (adjust constraint name if needed)
ALTER TABLE user_roles DROP FOREIGN KEY IF EXISTS FKhfh9dx7w3ubf1co1vdev94g3f;
ALTER TABLE user_roles DROP FOREIGN KEY IF EXISTS user_roles_ibfk_1;
ALTER TABLE user_roles DROP FOREIGN KEY IF EXISTS FK_user_roles_user_id;

-- Add new foreign key constraint with CASCADE DELETE
ALTER TABLE user_roles 
ADD CONSTRAINT FK_user_roles_user_id 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE; 