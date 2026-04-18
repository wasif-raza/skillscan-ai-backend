-- Add new columns
ALTER TABLE resume
ADD COLUMN expiry_time TIMESTAMP,
ADD COLUMN status VARCHAR(30),
ADD COLUMN retry_count INT DEFAULT 0;

-- Backfill existing data
UPDATE resume
SET expiry_time = DATE_ADD(uploaded_at, INTERVAL 24 HOUR),
    status = 'ACTIVE',
    retry_count = 0
WHERE expiry_time IS NULL;

-- Add index for cleanup job
CREATE INDEX idx_resume_cleanup
ON resume (expiry_time, status);