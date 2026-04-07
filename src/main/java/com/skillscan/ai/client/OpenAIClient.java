package com.skillscan.ai.client;

import org.springframework.stereotype.Component;

@Component
public class OpenAIClient {

    public String callAI(String prompt) {

        // TODO: Replace with real API call
        return """
        {
          "score": 85,
          "skills": ["Java", "Spring Boot", "SQL"],
          "keywords": ["Backend", "REST API"],
          "suggestions": ["Add more projects", "Improve summary"]
        }
        """;
    }
}