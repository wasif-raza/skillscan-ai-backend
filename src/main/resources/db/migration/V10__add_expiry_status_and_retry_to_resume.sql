-- Add new column
ALTER TABLE resumes
ADD COLUMN expiry_time TIMESTAMP,
ADD COLUMN status VARCHAR(30) DEFAULT 'ACTIVE',
ADD COLUMN retry_count INT DEFAULT 0;

-- Backfill existing data
UPDATE resumes
SET expiry_time = uploaded_at + INTERVAL '24 hours',
    status = 'ACTIVE',
    retry_count = 0
WHERE expiry_time IS NULL;

-- Add index
CREATE INDEX idx_resume_cleanup
ON resumes (expiry_time, status);
