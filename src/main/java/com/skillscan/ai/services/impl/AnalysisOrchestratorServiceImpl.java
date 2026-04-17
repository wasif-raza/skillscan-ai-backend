package com.skillscan.ai.services.impl;

import com.skillscan.ai.dto.request.AnalysisRequestDTO;
import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.exception.ResumeNotFoundException;
import com.skillscan.ai.metrics.SkillScanAIMetrics;
import com.skillscan.ai.model.Resume;
import com.skillscan.ai.repository.ResumeRepository;
import com.skillscan.ai.services.AnalysisOrchestratorService;
import com.skillscan.ai.services.LLMService;
import com.skillscan.ai.services.ResultMerger;
import com.skillscan.ai.services.ScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisOrchestratorServiceImpl implements AnalysisOrchestratorService {

    private final ResumeRepository resumeRepository;
    private final ScoringService scoringService;
    private final LLMService llmService;
    private final ResultMerger merger;
    private final SkillScanAIMetrics metrics;

    @Override
    public AIResponse analyze(AnalysisRequestDTO request) {

        //  Track total requests
        metrics.recordAnalysisRequest();

        return metrics.timeAnalysis(() -> {

            Resume resume = resumeRepository.findById(request.getResumeId())
                    .orElseThrow(() -> {
                        metrics.recordError("resume_not_found"); // 🔥 metric
                        return new ResumeNotFoundException("Resume not found");
                    });

            String resumeText = resume.getContent();
            String jd = request.getJobDescription();

            boolean isJDMode = jd != null && !jd.isBlank();

            log.info("Analysis started | resumeId={} | JD Mode={}",
                    request.getResumeId(), isJDMode);

            try {

                //  RULE SCORING
                AIResponse ruleResult = isJDMode
                        ? scoringService.calculateWithJD(resumeText, jd)
                        : scoringService.calculateWithoutJD(resumeText);

                //  LLM (optional)
                AIResponse llmResult = null;

                try {
                    llmResult = llmService.analyze(resumeText, jd);
                } catch (Exception e) {
                    log.warn("LLM failed, fallback to rule-based only");

                    //  Track LLM failure
                    metrics.recordError("llm_failure");
                }

                //  FALLBACK
                if (llmResult == null || llmResult.getLlmScore() <= 0) {

                    metrics.recordError("llm_fallback"); // track fallback usage

                    return AIResponse.builder()
                            .finalScore(ruleResult.getRuleScore())
                            .ruleScore(ruleResult.getRuleScore())
                            .llmScore(0)
                            .skills(ruleResult.getSkills())
                            .matchedKeywords(ruleResult.getMatchedKeywords())
                            .missingKeywords(ruleResult.getMissingKeywords())
                            .suggestions(Collections.emptyList())
                            .build();
                }

                //  MERGE
                return merger.merge(ruleResult, llmResult);

            } catch (Exception e) {

                //  Track unexpected failures
                metrics.recordError("analysis_failure");

                log.error("Analysis failed", e);
                throw e;
            }
        });
    }
}