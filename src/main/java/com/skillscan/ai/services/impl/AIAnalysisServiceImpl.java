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
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AIAnalysisServiceImpl implements AIAnalysisService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final OpenAIClient openAIClient;
    private final AIResponseMapper aiResponseMapper;

    //  Lock per resumeId
    private final ConcurrentHashMap<UUID, Object> locks = new ConcurrentHashMap<>();

    @Override
    public AIResponse analyze(UUID resumeId) {

        //  Fast path (cache check)
        return resumeAnalysisRepository.findByResumeId(resumeId)
                .map(aiResponseMapper::mapToResponse)
                .orElseGet(() -> processWithLock(resumeId));
    }

    private AIResponse processWithLock(UUID resumeId) {

        Object lock = locks.computeIfAbsent(resumeId, k -> new Object());

        synchronized (lock) {
            try {
                //  Double-check inside lock
                return resumeAnalysisRepository.findByResumeId(resumeId)
                        .map(aiResponseMapper::mapToResponse)
                        .orElseGet(() -> {

                            Resume resume = resumeRepository.findById(resumeId)
                                    .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

                            String content = resume.getContent();
                            if (content == null || content.isBlank()) {
                                throw new IllegalStateException("Resume content is empty or not yet parsed");
                            }

                            String prompt = buildPrompt(content);

                            String aiRawResponse = openAIClient.callAI(prompt);

                            AIResponse aiResponse =
                                    aiResponseMapper.mapToAIResponse(aiRawResponse);

                            //  Save to DB
                            ResumeAnalysis entity =
                                    aiResponseMapper.mapToEntity(resumeId, aiResponse);

                            resumeAnalysisRepository.save(entity);

                            return aiResponse;
                        });

            } finally {
                //  Prevent memory leak
                locks.remove(resumeId);
            }
        }
    }

    private String buildPrompt(String resumeText) {

        //  Limit text size
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