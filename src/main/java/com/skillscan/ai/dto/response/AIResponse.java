package com.skillscan.ai.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AIResponse {
    private int score;
    private List<String> skills;
    private List<String> suggestions;
    private List<String> keywords;
}
