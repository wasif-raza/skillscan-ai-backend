package com.skillscan.ai.services.impl;

import com.skillscan.ai.client.OpenAIClient;
import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.exception.ResourceNotFoundException;
import com.skillscan.ai.mapper.AIResponseMapper;
import com.skillscan.ai.model.Resume;
import com.skillscan.ai.model.ResumeAnalysis;
import com.skillscan.ai.repository.ResumeAnalysisRepository;
import com.skillscan.ai.repository.ResumeRepository;
import com.skillscan.ai.services.AIAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIAnalysisServiceImpl implements AIAnalysisService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final OpenAIClient openAIClient;
    private final AIResponseMapper aiResponseMapper;

    private final ConcurrentHashMap<UUID, CompletableFuture<AIResponse>> inProgress = new ConcurrentHashMap<>();

    @Override
    public AIResponse analyze(UUID resumeId) {

        //  Cache check
        return resumeAnalysisRepository.findByResumeId(resumeId)
                .map(entity -> {
                    log.info("Returning cached analysis for resumeId={}", resumeId);
                    return aiResponseMapper.mapToResponse(entity);
                })
                .orElseGet(() -> processAsync(resumeId));
    }

    private AIResponse processAsync(UUID resumeId) {

        CompletableFuture<AIResponse> future =
                inProgress.computeIfAbsent(resumeId, id -> {
                    log.info("Starting AI processing for resumeId={}", id);
                    return CompletableFuture.supplyAsync(() -> generateAndSave(id));
                });

        try {
            return future.get();
        } catch (Exception e) {
            log.error("AI processing failed for resumeId={}", resumeId, e);
            throw new RuntimeException("AI processing failed", e);
        } finally {
            //  safe removal (prevents race condition)
            inProgress.computeIfPresent(resumeId, (k, v) -> v == future ? null : v);
        }
    }

    private AIResponse generateAndSave(UUID resumeId) {

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        String content = resume.getContent();

        if (content == null || content.isBlank()) {
            log.error("Empty resume content for resumeId={}", resumeId);
            throw new IllegalStateException("Resume content is empty or not yet parsed");
        }

        String prompt = buildPrompt(content);

        try {
            log.info("Calling AI for resumeId={}", resumeId);

            AIResponse aiResponse = openAIClient.callAI(prompt);

            //  Optional validation
            if (aiResponse.getScore() == 0 &&
                    aiResponse.getSkills().isEmpty() &&
                    aiResponse.getKeywords().isEmpty()) {

                log.warn("Weak/invalid AI response for resumeId={}", resumeId);
            }

            ResumeAnalysis entity =
                    aiResponseMapper.mapToEntity(resumeId, aiResponse);

            resumeAnalysisRepository.save(entity);

            log.info("AI analysis saved for resumeId={}", resumeId);

            return aiResponse;

        } catch (Exception e) {
            log.error("AI generation failed for resumeId={}", resumeId, e);

            AIResponse fallback = new AIResponse();
            fallback.setScore(0);
            fallback.getSuggestions().add("AI analysis failed. Try again.");

            return fallback;
        }
    }

    private String buildPrompt(String resumeText) {

        String trimmed = resumeText.length() > 4000
                ? resumeText.substring(0, 4000)
                : resumeText;

        return """
            You are a strict ATS resume analyzer.
            
            You MUST follow ALL rules below strictly:
            
            RULES:
            1. Output ONLY valid JSON
            2. Do NOT include any explanation
            3. Do NOT include markdown (no ```json)
            4. Do NOT include comments
            5. Do NOT include text before or after JSON
            6. All fields must exist
            7. skills, keywords, suggestions MUST be arrays of strings
            8. score MUST be an integer between 0 and 100
            
            OUTPUT FORMAT:
            {
              "score": 0,
              "skills": ["string"],
              "keywords": ["string"],
              "suggestions": ["string"]
            }
            
            If you are unsure or cannot analyze, return EXACTLY:
            {
              "score": 0,
              "skills": [],
              "keywords": [],
              "suggestions": ["error"]
            }
            
            Now analyze the following resume:
            
            """ + trimmed;
    }
}