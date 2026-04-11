package com.skillscan.ai.services.impl;

import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.services.ResultMerger;
import org.springframework.stereotype.Service;

@Service
public class ResultMergerImpl implements ResultMerger {

    @Override
    public AIResponse merge(AIResponse rule, AIResponse llm) {

        double finalScore =
                (rule.getRuleScore() * 0.7) +
                        (llm.getLlmScore() * 0.3);
        boolean llmUsed = llm != null && llm.getLlmScore() > 0;

        return AIResponse.builder()
                .finalScore(finalScore)
                .ruleScore(rule.getRuleScore())
                .llmScore(llm.getLlmScore())
                .skills(rule.getSkills())
                .matchedKeywords(rule.getMatchedKeywords())
                .missingKeywords(rule.getMissingKeywords())
                .suggestions(llm.getSuggestions())
                .build();
    }
}