package com.skillscan.ai.unitTest;

import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.services.impl.ResultMergerImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultMergerImplTest {

    private final ResultMergerImpl merger = new ResultMergerImpl();

    //  Normal merge case
    @Test
    void shouldMergeRuleAndLLMCorrectly() {

        AIResponse rule = AIResponse.builder()
                .ruleScore(80)
                .skills(List.of("Java"))
                .matchedKeywords(List.of("Spring"))
                .missingKeywords(List.of("Docker"))
                .build();

        AIResponse llm = AIResponse.builder()
                .llmScore(60)
                .suggestions(List.of("Improve projects"))
                .build();

        AIResponse result = merger.merge(rule, llm);

        // 0.7 * 80 + 0.3 * 60 = 74
        assertEquals(74.0, result.getFinalScore());

        assertEquals(80, result.getRuleScore());
        assertEquals(60, result.getLlmScore());

        assertEquals(List.of("Java"), result.getSkills());
        assertEquals(List.of("Spring"), result.getMatchedKeywords());
        assertEquals(List.of("Docker"), result.getMissingKeywords());

        assertEquals(List.of("Improve projects"), result.getSuggestions());
    }

    //  LLM score = 0
    @Test
    void shouldHandleZeroLLMScore() {

        AIResponse rule = AIResponse.builder()
                .ruleScore(70)
                .build();

        AIResponse llm = AIResponse.builder()
                .llmScore(0)
                .suggestions(List.of("Fallback suggestion"))
                .build();

        AIResponse result = merger.merge(rule, llm);

        // 0.7 * 70 + 0.3 * 0 = 49
        assertEquals(49.0, result.getFinalScore());
        assertEquals(0, result.getLlmScore());
    }

    //  Different values
    @Test
    void shouldCalculateWeightedScoreAccurately() {

        AIResponse rule = AIResponse.builder()
                .ruleScore(50)
                .build();

        AIResponse llm = AIResponse.builder()
                .llmScore(100)
                .build();

        AIResponse result = merger.merge(rule, llm);

        // 0.7 * 50 + 0.3 * 100 = 65
        assertEquals(65.0, result.getFinalScore());
    }

    // Edge case: empty lists
    @Test
    void shouldHandleEmptyLists() {

        AIResponse rule = AIResponse.builder()
                .ruleScore(60)
                .skills(List.of())
                .matchedKeywords(List.of())
                .missingKeywords(List.of())
                .build();

        AIResponse llm = AIResponse.builder()
                .llmScore(40)
                .suggestions(List.of())
                .build();

        AIResponse result = merger.merge(rule, llm);

        assertTrue(result.getSkills().isEmpty());
        assertTrue(result.getMatchedKeywords().isEmpty());
        assertTrue(result.getMissingKeywords().isEmpty());
        assertTrue(result.getSuggestions().isEmpty());
    }
}