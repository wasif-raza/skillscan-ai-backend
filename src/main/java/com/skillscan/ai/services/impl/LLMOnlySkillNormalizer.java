package com.skillscan.ai.services.impl;

import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.services.LLMService;
import com.skillscan.ai.services.SkillNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class LLMOnlySkillNormalizer implements SkillNormalizer {

    private final LLMService llmService;

    @Override
    public List<String> normalize(List<String> skills) {

        if (skills == null || skills.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> cleanedSkills = skills.stream()
                .map(s -> s.toLowerCase().trim())
                .filter(s -> !s.isEmpty())
                .sorted()
                .toList();

        try {
            String prompt = """
                    Convert the following skills into standard professional keywords.
                    - Merge similar terms
                    - Use industry-standard naming
                    - Keep it concise list
                    
                    Skills: %s
                    """.formatted(cleanedSkills);

            AIResponse response = llmService.analyze(prompt, "");

            if (response.getSkills() != null && !response.getSkills().isEmpty()) {

                return response.getSkills().stream()
                        .map(s -> s.toLowerCase().trim())
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .toList();

            }
        } catch (Exception e) {
            log.error("LLM normalization failed", e);
        }

        //  fallback clean
        return cleanedSkills;
    }
}