package com.skillscan.ai.services.impl;

import com.skillscan.ai.client.OpenAIClient;
import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.services.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMServiceImpl implements LLMService {

    private final OpenAIClient client;

    @Override
    public AIResponse analyze(String resumeText, String jd) {

        log.info("Calling LLM | resumeLength={} | jdPresent={}",
                resumeText != null ? resumeText.length() : 0,
                jd != null && !jd.isBlank());

        String prompt = """
        You are an AI resume analyzer.

        TASK:
        Analyze the resume and (if provided) job description.

        Return ONLY valid JSON (no extra text) in this format:

        {
          "score": number (0-100),
          "skills": [list of standardized skills],
          "suggestions": [list of improvements]
        }

        RULES:
        - Normalize all skills to industry-standard keywords
        - Merge duplicates (ReactJS → React, Spring Boot → Spring)
        - Support ALL domains (software, mechanical, electrical, etc.)
        - Keep output concise
        - Do NOT include explanation outside JSON

        RESUME:
        %s

        JOB DESCRIPTION:
        %s
        """.formatted(
                resumeText == null ? "" : resumeText,
                jd == null ? "" : jd
        );

        try {
            AIResponse response = client.callAI(prompt);

            if (response == null) {
                return fallback();
            }

            return response;

        } catch (Exception e) {
            log.error("LLMService failed", e);
            return fallback();
        }
    }

    private AIResponse fallback() {
        return AIResponse.builder()
                .llmScore(0)
                .skills(null)
                .suggestions(List.of("AI analysis failed"))
                .build();
    }
}