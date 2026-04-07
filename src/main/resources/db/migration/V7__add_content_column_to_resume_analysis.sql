CREATE TABLE resume_analysis (
    id UUID PRIMARY KEY,
    resume_id UUID UNIQUE NOT NULL,
    score INT,
    skills_json TEXT,
    keywords_json TEXT,
    suggestions_json TEXT
);