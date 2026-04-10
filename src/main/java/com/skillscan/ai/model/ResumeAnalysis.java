package com.skillscan.ai.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "resume_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID resumeId;

    private double finalScore;
    private double ruleScore;
    private double llmScore;

    @ElementCollection
    private List<String> suggestions;

    private LocalDateTime createdAt;

    // 🔥 AUTO SET TIMESTAMP
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}