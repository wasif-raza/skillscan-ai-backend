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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisOrchestratorServiceImpl
        implements AnalysisOrchestratorService {

    private final ResumeRepository resumeRepository;

    private final ScoringService scoringService;

    private final LLMService llmService;

    private final ResultMerger merger;

    private final SkillScanAIMetrics metrics;

    @Override
    public AIResponse analyze(
            AnalysisRequestDTO request,
            boolean guest
    ) {

        metrics.recordAnalysisRequest();

        return metrics.timeAnalysis(() -> {

            Resume resume =
                    resumeRepository
                            .findById(
                                    request.getResumeId()
                            )

                            .orElseThrow(() -> {

                                metrics.recordError(
                                        "resume_not_found"
                                );

                                return new ResumeNotFoundException(
                                        "Resume not found"
                                );

                            });

            String resumeText =
                    resume.getContent();

            String jd =
                    request.getJobDescription();

            boolean isJDMode =
                    jd != null
                            &&
                            !jd.isBlank();

            log.info(
                    "Analysis started | resumeId={} | JD={} | guest={}",
                    request.getResumeId(),
                    isJDMode,
                    guest
            );

            try {

                AIResponse ruleResult =
                        isJDMode

                                ? scoringService
                                .calculateWithJD(
                                        resumeText,
                                        jd
                                )

                                : scoringService
                                .calculateWithoutJD(
                                        resumeText
                                );

                AIResponse llmResult =
                        null;

                try {

                    llmResult =
                            llmService.analyze(
                                    resumeText,
                                    jd
                            );

                }

                catch (Exception e) {

                    log.warn(
                            "LLM failed, fallback"
                    );

                    metrics.recordError(
                            "llm_failure"
                    );

                }


                // FALLBACK

                if (

                        llmResult == null

                                ||

                                llmResult
                                        .getLlmScore()
                                        <= 0

                ) {

                    metrics.recordError(
                            "llm_fallback"
                    );

                    AIResponse fallback =
                            AIResponse.builder()

                                    .finalScore(
                                            ruleResult
                                                    .getRuleScore()
                                    )

                                    .ruleScore(
                                            ruleResult
                                                    .getRuleScore()
                                    )

                                    .llmScore(
                                            0
                                    )

                                    .skills(
                                            ruleResult
                                                    .getSkills()
                                    )

                                    .matchedKeywords(
                                            ruleResult
                                                    .getMatchedKeywords()
                                    )

                                    .missingKeywords(
                                            ruleResult
                                                    .getMissingKeywords()
                                    )

                                    .suggestions(
                                            Collections.emptyList()
                                    )

                                    .build();

                    return applyGuestRules(
                            fallback,
                            guest
                    );

                }


                // MERGE

                AIResponse response =
                        merger.merge(
                                ruleResult,
                                llmResult
                        );

                return applyGuestRules(
                        response,
                        guest
                );

            }

            catch (Exception e) {

                if (
                        !(
                                e instanceof
                                        ResumeNotFoundException
                        )
                ) {

                    metrics.recordError(
                            "analysis_failure"
                    );

                    log.error(
                            "Analysis failed",
                            e
                    );

                }

                throw e;

            }

        });

    }


    private AIResponse applyGuestRules(
            AIResponse response,
            boolean guest
    ) {

        if (!guest) {

            response.setGuest(
                    false
            );

            response.setLockedSuggestions(
                    false
            );

            response.setLockedKeywords(
                    false
            );

            response.setHiddenSuggestions(
                    0
            );

            response.setHiddenKeywords(
                    0
            );

            return response;

        }


        List<String> suggestions =
                response.getSuggestions()
                        == null

                        ? Collections.emptyList()

                        : response
                        .getSuggestions();

        List<String> keywords =
                response.getMissingKeywords()
                        == null

                        ? Collections.emptyList()

                        : response
                        .getMissingKeywords();


        int totalSuggestions =
                suggestions.size();

        int totalKeywords =
                keywords.size();


        response.setSuggestions(

                suggestions
                        .stream()
                        .limit(2)
                        .toList()

        );


        response.setMissingKeywords(

                keywords
                        .stream()
                        .limit(2)
                        .toList()

        );


        response.setGuest(
                true
        );

        response.setLockedSuggestions(
                totalSuggestions > 2
        );

        response.setLockedKeywords(
                totalKeywords > 2
        );

        response.setHiddenSuggestions(
                Math.max(
                        0,
                        totalSuggestions - 2
                )
        );

        response.setHiddenKeywords(
                Math.max(
                        0,
                        totalKeywords - 2
                )
        );

        return response;

    }

}