package com.skillscan.ai.services;

import com.skillscan.ai.dto.response.AIResponse;

public interface ScoringService {

    AIResponse calculateWithJD(String resumeText, String jobDescription);

    AIResponse calculateWithoutJD(String resumeText);
}