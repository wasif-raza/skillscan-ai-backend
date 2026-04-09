package com.skillscan.ai.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.model.ResumeAnalysis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AIResponseMapper {

    private final ObjectMapper objectMapper;

    //  DTO → Entity
    public ResumeAnalysis mapToEntity(UUID resumeId, AIResponse response) {
        try {
            return ResumeAnalysis.builder()
                    .resumeId(resumeId)
                    .score(response.getScore())
                    .skillsJson(toJson(response.getSkills()))
                    .keywordsJson(toJson(response.getKeywords()))
                    .suggestionsJson(toJson(response.getSuggestions()))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to map AIResponse to entity", e);
        }
    }

    //  Entity → DTO
    public AIResponse mapToResponse(ResumeAnalysis entity) {
        try {
            AIResponse response = new AIResponse();
            response.setScore(entity.getScore());

            response.setSkills(fromJson(entity.getSkillsJson()));
            response.setKeywords(fromJson(entity.getKeywordsJson()));
            response.setSuggestions(fromJson(entity.getSuggestionsJson()));

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to map entity to AIResponse", e);
        }
    }

    //  Convert List → JSON String (safe)
    private String toJson(List<String> list) throws JsonProcessingException {
        return objectMapper.writeValueAsString(
                list != null ? list : Collections.emptyList()
        );
    }

    //  Convert JSON String → List (safe)
    private List<String> fromJson(String json) throws JsonProcessingException {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        return objectMapper.readValue(json, new TypeReference<List<String>>() {});
    }
}