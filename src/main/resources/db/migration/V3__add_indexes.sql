CREATE INDEX idx_resume_cleanup
ON resumes (expiry_time, status);