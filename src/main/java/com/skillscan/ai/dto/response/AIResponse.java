package com.skillscan.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {

    // 🔥 Final output
    private double finalScore;

    // 🔥 Internal scores
    private double ruleScore;
    private double llmScore;

    // 🔥 Skills
    private List<String> skills;

    // 🔥 Matching (only for JD mode)
    private List<String> matchedKeywords;
    private List<String> missingKeywords;

    // 🔥 Suggestions (mostly from LLM)
    private List<String> suggestions;

    // 🔥 NEW (IMPORTANT)
    private boolean llmUsed;
}