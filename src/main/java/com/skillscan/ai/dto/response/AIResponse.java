package com.skillscan.ai.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AIResponse {
    private int score;
    private List<String> skills = new ArrayList<>() ;
    private List<String> suggestions = new ArrayList<>();
    private List<String> keywords = new ArrayList<>();
}
