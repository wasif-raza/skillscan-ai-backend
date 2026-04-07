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

    // AI JSON → DTO
    public AIResponse mapToAIResponse(String aiRawResponse) {
        try {
            return objectMapper.readValue(aiRawResponse, AIResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }

    // DTO → Entity
    public ResumeAnalysis mapToEntity(UUID resumeId, AIResponse response) {
        try {
            return ResumeAnalysis.builder()
                    .resumeId(resumeId)
                    .score(response.getScore())
                    .skillsJson(objectMapper.writeValueAsString(response.getSkills() != null ? response.getSkills() : Collections.emptyList()))
                    .keywordsJson(objectMapper.writeValueAsString(response.getKeywords() != null ? response.getKeywords() : Collections.emptyList()))
                    .suggestionsJson(objectMapper.writeValueAsString(response.getSuggestions() != null ? response.getSuggestions() : Collections.emptyList()))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to map AIResponse to entity", e);
        }
    }

    // Entity → DTO
    public AIResponse mapToResponse(ResumeAnalysis entity) {
        try {
            AIResponse response = new AIResponse();
            response.setScore(entity.getScore());

            response.setSkills(objectMapper.readValue(
                    entity.getSkillsJson(), new TypeReference<List<String>>() {}));

            response.setKeywords(objectMapper.readValue(
                    entity.getKeywordsJson(), new TypeReference<List<String>>() {}));

            response.setSuggestions(objectMapper.readValue(
                    entity.getSuggestionsJson(), new TypeReference<List<String>>() {}));

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map entity to AIResponse", e);
        }
    }
}