package com.skillscan.ai.service.impl;

import com.skillscan.ai.client.OpenAIClient;
import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.mapper.AIResponseMapper;
import com.skillscan.ai.model.Resume;
import com.skillscan.ai.model.ResumeAnalysis;
import com.skillscan.ai.repository.ResumeAnalysisRepository;
import com.skillscan.ai.repository.ResumeRepository;
import com.skillscan.ai.services.AIAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AIAnalysisServiceImpl implements AIAnalysisService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final OpenAIClient openAIClient;
    private final AIResponseMapper aiResponseMapper;

    @Override
    public AIResponse analyze(UUID resumeId) {

        // 1 Check DB (CACHE)
        return resumeAnalysisRepository.findByResumeId(resumeId)
                .map(aiResponseMapper::mapToResponse)

                //  If not found call AI
                .orElseGet(() -> {

                    Resume resume = resumeRepository.findById(resumeId)
                            .orElseThrow(() -> new RuntimeException("Resume not found"));

                    String prompt = buildPrompt(resume.getContent());

                    String aiRawResponse = openAIClient.callAI(prompt);

                    AIResponse aiResponse =
                            aiResponseMapper.mapToAIResponse(aiRawResponse);

                    // 3️Save to DB
                    ResumeAnalysis entity =
                            aiResponseMapper.mapToEntity(resumeId, aiResponse);

                    resumeAnalysisRepository.save(entity);

                    return aiResponse;
                });
    }

    private String buildPrompt(String resumeText) {

        //  limit text size (VERY IMPORTANT)
        String trimmed = resumeText.length() > 4000
                ? resumeText.substring(0, 4000)
                : resumeText;

        return """
        You are an ATS resume analyzer.

        Analyze and return STRICT JSON:
        {
          "score": number,
          "skills": [],
          "keywords": [],
          "suggestions": []
        }

        Resume:
        """ + trimmed;
    }
}