#  SkillScan AI – Backend

AI-powered Resume Analyzer built with Spring Boot, PostgreSQL, and Docker.

---

##  Overview

SkillScan AI is a backend system that analyzes resumes using AI techniques to:

* Extract skills
* Calculate ATS score
* Identify missing skills
* Generate improvement suggestions

---

##  Architecture Flow

User Upload Resume → Backend API → Resume Parsing → AI Analysis → Database Storage → Response

---

## ⚙️ Tech Stack

* Java 17
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Docker
* Apache Tika (for parsing)

---

##  Features

* Resume Upload API
* Text Extraction from PDF/DOCX
* Skill Detection
* ATS Score Calculation
* Suggestions Generation
* RESTful APIs

---

##  Database Design

### Users

* id (UUID)
* name
* email
* password

### Resumes

* id
* file_name
* extracted_text

### Resume Analysis

* score
* skills
* missing_skills
* suggestions


##  Future Enhancements

* AI-based resume rewriting
* Job description matching
* Authentication (JWT)
* Cloud deployment

---

##  Author

Wasif Raza
B.Tech CSE | Java Full Stack Developer

---

##  Project Status

 In Development
