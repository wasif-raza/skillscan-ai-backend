-- V3__fix_email_constraints_and_data_integrity.sql

-- Normalize emails to lowercase
UPDATE users
SET email = LOWER(email);

-- Add NOT NULL
ALTER TABLE users
ALTER COLUMN email SET NOT NULL;
