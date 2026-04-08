package com.skillscan.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AIResponse {
    private int score = 0;
    private List<String> skills = new ArrayList<>() ;
    private List<String> suggestions = new ArrayList<>();
    private List<String> keywords = new ArrayList<>();
}
