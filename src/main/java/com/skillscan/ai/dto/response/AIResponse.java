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

    private double finalScore;

    private double ruleScore;

    private double llmScore;

    private List<String> skills;

    private List<String> matchedKeywords;

    private List<String> missingKeywords;

    private List<String> suggestions;


    // guest/full mode

    private boolean guest;


    // frontend lock UI

    private boolean lockedSuggestions;

    private boolean lockedKeywords;


    // hidden counts

    private Integer hiddenSuggestions;

    private Integer hiddenKeywords;

}