package com.skillscan.ai.services;

import com.skillscan.ai.dto.response.AIResponse;

public interface LLMService {

    AIResponse analyze(String resumeText, String jobDescription);
}