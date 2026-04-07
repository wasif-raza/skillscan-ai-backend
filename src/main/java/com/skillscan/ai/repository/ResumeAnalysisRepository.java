package com.skillscan.ai.repository;

import com.skillscan.ai.model.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, UUID>{

    Optional<ResumeAnalysis>findByResumeId(UUID resumeId);

}
