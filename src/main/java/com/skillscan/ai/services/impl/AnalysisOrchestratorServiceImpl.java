package com.skillscan.ai.services.impl;

import com.skillscan.ai.dto.request.AnalysisRequestDTO;
import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.model.Resume;
import com.skillscan.ai.repository.ResumeRepository;
import com.skillscan.ai.services.AnalysisOrchestratorService;
import com.skillscan.ai.services.LLMService;
import com.skillscan.ai.services.ResultMerger;
import com.skillscan.ai.services.ScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisOrchestratorServiceImpl implements AnalysisOrchestratorService {

    private final ResumeRepository resumeRepository;
    private final ScoringService scoringService;
    private final LLMService llmService;
    private final ResultMerger merger;

    @Override
    public AIResponse analyze(AnalysisRequestDTO request) {

        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        String resumeText = resume.getContent();
        String jd = request.getJobDescription();

        boolean isJDMode = jd != null && !jd.isBlank();

        log.info("Analysis started | resumeId={} | JD Mode={}", request.getResumeId(), isJDMode);

        // RULE SCORING (ALWAYS SAFE)
        AIResponse ruleResult = isJDMode
                ? scoringService.calculateWithJD(resumeText, jd)
                : scoringService.calculateWithoutJD(resumeText);

        //  LLM (OPTIONAL)
        AIResponse llmResult = null;

        try {
            llmResult = llmService.analyze(resumeText, jd);
        } catch (Exception e) {
            log.warn("LLM failed, fallback to rule-based only");
        }

        //  FALLBACK
        if (llmResult == null || llmResult.getLlmScore() <= 0) {
            return AIResponse.builder()
                    .finalScore(ruleResult.getRuleScore())
                    .ruleScore(ruleResult.getRuleScore())
                    .llmScore(0)
                    .skills(ruleResult.getSkills())
                    .matchedKeywords(ruleResult.getMatchedKeywords())
                    .missingKeywords(ruleResult.getMissingKeywords())
                    .suggestions(ruleResult.getSuggestions())
                    .llmUsed(false)
                    .build();
        }

        //  MERGE
        return merger.merge(ruleResult, llmResult);
    }
}