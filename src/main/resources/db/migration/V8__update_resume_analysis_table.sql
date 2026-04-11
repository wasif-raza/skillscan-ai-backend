--  Add new columns for hybrid scoring
ALTER TABLE resume_analysis
ADD COLUMN final_score DOUBLE PRECISION,
ADD COLUMN rule_score DOUBLE PRECISION,
ADD COLUMN llm_score DOUBLE PRECISION;

--  Add suggestions (stored as TEXT or JSON)
ALTER TABLE resume_analysis
ADD COLUMN suggestions TEXT;

--  Add created_at timestamp
ALTER TABLE resume_analysis
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

--  Remove old columns (LLM-only design)
ALTER TABLE resume_analysis
DROP COLUMN score,
DROP COLUMN skills_json,
DROP COLUMN keywords_json,
DROP COLUMN suggestions_json;

--  Remove unique constraint on resume_id (important for history)
ALTER TABLE resume_analysis
DROP CONSTRAINT IF EXISTS resume_analysis_resume_id_key;