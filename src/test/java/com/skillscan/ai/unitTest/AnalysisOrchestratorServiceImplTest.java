package com.skillscan.ai.unitTest;

import com.skillscan.ai.dto.request.AnalysisRequestDTO;
import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.exception.ResumeNotFoundException;
import com.skillscan.ai.metrics.SkillScanAIMetrics;
import com.skillscan.ai.model.Resume;
import com.skillscan.ai.repository.ResumeRepository;
import com.skillscan.ai.services.LLMService;
import com.skillscan.ai.services.ResultMerger;
import com.skillscan.ai.services.ScoringService;
import com.skillscan.ai.services.impl.AnalysisOrchestratorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AnalysisOrchestratorServiceImplTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private ScoringService scoringService;

    @Mock
    private LLMService llmService;

    @Mock
    private ResultMerger merger;

    @Mock
    private SkillScanAIMetrics metrics;

    @InjectMocks
    private AnalysisOrchestratorServiceImpl orchestrator;

    private UUID resumeId;
    private Resume resume;

    @BeforeEach
    void setup() {
        resumeId = UUID.randomUUID();

        resume = new Resume();
        resume.setContent("Java Spring Boot Developer");

        // VERY IMPORTANT → execute supplier inside metrics wrapper
        when(metrics.timeAnalysis(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
    }

    //  Resume Not Found
    @Test
    void shouldThrowResumeNotFoundException() {

        when(resumeRepository.findById(resumeId)).thenReturn(Optional.empty());

        AnalysisRequestDTO request = new AnalysisRequestDTO();
        request.setResumeId(resumeId);

        assertThrows(ResumeNotFoundException.class, () ->
                orchestrator.analyze(request)
        );

        verify(metrics).recordError("resume_not_found");
    }

    //  Resume Only (No JD)
    @Test
    void shouldAnalyzeWithoutJD() {

        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));

        AIResponse ruleResponse = AIResponse.builder()
                .ruleScore(70)
                .build();

        AIResponse llmResponse = AIResponse.builder()
                .llmScore(80)
                .build();

        AIResponse merged = AIResponse.builder()
                .finalScore(75)
                .build();

        when(scoringService.calculateWithoutJD(any())).thenReturn(ruleResponse);
        when(llmService.analyze(any(), any())).thenReturn(llmResponse);
        when(merger.merge(ruleResponse, llmResponse)).thenReturn(merged);

        AnalysisRequestDTO request = new AnalysisRequestDTO();
        request.setResumeId(resumeId);

        AIResponse result = orchestrator.analyze(request);

        assertEquals(75, result.getFinalScore());

        verify(scoringService).calculateWithoutJD(any());
        verify(llmService).analyze(any(), any());
        verify(merger).merge(ruleResponse, llmResponse);
    }

    //  Resume + JD
    @Test
    void shouldAnalyzeWithJD() {

        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));

        AIResponse ruleResponse = AIResponse.builder().ruleScore(60).build();
        AIResponse llmResponse = AIResponse.builder().llmScore(70).build();
        AIResponse merged = AIResponse.builder().finalScore(65).build();

        when(scoringService.calculateWithJD(any(), any())).thenReturn(ruleResponse);
        when(llmService.analyze(any(), any())).thenReturn(llmResponse);
        when(merger.merge(ruleResponse, llmResponse)).thenReturn(merged);

        AnalysisRequestDTO request = new AnalysisRequestDTO();
        request.setResumeId(resumeId);
        request.setJobDescription("Spring Boot JD");

        AIResponse result = orchestrator.analyze(request);

        assertEquals(65, result.getFinalScore());

        verify(scoringService).calculateWithJD(any(), any());
    }

    //  LLM Failure → fallback to rule only
    @Test
    void shouldFallbackWhenLLMFails() {

        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));

        AIResponse ruleResponse = AIResponse.builder()
                .ruleScore(50)
                .skills(java.util.List.of("Java"))
                .build();

        when(scoringService.calculateWithoutJD(any())).thenReturn(ruleResponse);

        when(llmService.analyze(any(), any()))
                .thenThrow(new IllegalStateException("LLM down"));

        AnalysisRequestDTO request = new AnalysisRequestDTO();
        request.setResumeId(resumeId);

        AIResponse result = orchestrator.analyze(request);

        assertEquals(50, result.getFinalScore());
        assertEquals(0, result.getLlmScore());

        verify(metrics).recordError("llm_failure");
        verify(metrics).recordError("llm_fallback");
    }

    //  LLM returns invalid score → fallback
    @Test
    void shouldFallbackWhenLLMScoreInvalid() {

        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));

        AIResponse ruleResponse = AIResponse.builder()
                .ruleScore(55)
                .build();

        AIResponse llmResponse = AIResponse.builder()
                .llmScore(0) // invalid
                .build();

        when(scoringService.calculateWithoutJD(any())).thenReturn(ruleResponse);
        when(llmService.analyze(any(), any())).thenReturn(llmResponse);

        AnalysisRequestDTO request = new AnalysisRequestDTO();
        request.setResumeId(resumeId);

        AIResponse result = orchestrator.analyze(request);

        assertEquals(55, result.getFinalScore());

        verify(metrics).recordError("llm_fallback");
    }

    //  Unexpected failure → metrics + rethrow
    @Test
    void shouldRecordErrorAndRethrowIfUnexpectedFailure() {

        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));

        when(scoringService.calculateWithoutJD(any()))
                .thenThrow(new IllegalStateException("Scoring failed"));

        AnalysisRequestDTO request = new AnalysisRequestDTO();
        request.setResumeId(resumeId);

        assertThrows(IllegalStateException.class, () ->
                orchestrator.analyze(request)
        );

        verify(metrics).recordError("analysis_failure");
    }
}