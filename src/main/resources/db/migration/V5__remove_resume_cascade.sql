-- Remove existing foreign key constraint
ALTER TABLE resumes
DROP CONSTRAINT IF EXISTS resumes_user_id_fkey;

-- Recreate without cascade
ALTER TABLE resumes
ADD CONSTRAINT resumes_user_id_fkey
FOREIGN KEY (user_id)
REFERENCES users(id);