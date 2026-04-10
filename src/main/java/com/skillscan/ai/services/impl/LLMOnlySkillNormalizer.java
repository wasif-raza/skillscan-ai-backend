package com.skillscan.ai.services.impl;

import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.services.LLMService;
import com.skillscan.ai.services.SkillNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMOnlySkillNormalizer implements SkillNormalizer {

    private final LLMService llmService;

    // ✅ cache
    private final Map<String, List<String>> cache = new HashMap<>();

    @Override
    public List<String> normalize(List<String> skills) {

        if (skills == null || skills.isEmpty()) {
            return Collections.emptyList();
        }

        String key = skills.toString();

        // ✅ 1. Cache check
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        try {
            String prompt = """
                Convert the following skills into standard professional keywords.
                - Merge similar terms
                - Use industry-standard naming
                - Keep it concise list

                Skills: %s
                """.formatted(skills);

            AIResponse response = llmService.analyze(prompt, "");

            if (response.getSkills() != null && !response.getSkills().isEmpty()) {

                List<String> normalized = response.getSkills().stream()
                        .map(s -> s.toLowerCase().trim())
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .toList();

                cache.put(key, normalized); // ✅ store cache
                return normalized;
            }

        } catch (Exception e) {
            log.warn("LLM normalization failed, fallback used");
        }

        // ✅ fallback clean
        return skills.stream()
                .map(s -> s.toLowerCase().trim())
                .distinct()
                .toList();
    }
}