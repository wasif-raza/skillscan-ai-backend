package com.skillscan.ai.services;

import com.skillscan.ai.dto.response.AIResponse;

public interface ResultMerger {
    AIResponse merge(AIResponse rule, AIResponse llm);
}