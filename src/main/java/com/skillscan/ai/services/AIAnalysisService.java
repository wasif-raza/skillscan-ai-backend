package com.skillscan.ai.services;

import com.skillscan.ai.dto.response.AIResponse;

import java.util.UUID;

public interface AIAnalysisService {
    AIResponse analyze(UUID resumeId);
}