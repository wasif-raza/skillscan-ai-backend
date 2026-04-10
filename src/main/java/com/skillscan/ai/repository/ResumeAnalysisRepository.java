package com.skillscan.ai.repository;

import com.skillscan.ai.model.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, UUID> {

    // ✅ Get latest analysis for a resume
    Optional<ResumeAnalysis> findTopByResumeIdOrderByCreatedAtDesc(UUID resumeId);

    // ✅ Get all analysis history for a resume
    List<ResumeAnalysis> findByResumeIdOrderByCreatedAtDesc(UUID resumeId);

    // ✅ Optional: delete old analyses (cleanup)
    void deleteByResumeId(UUID resumeId);
}