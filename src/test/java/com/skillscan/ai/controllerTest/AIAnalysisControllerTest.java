package com.skillscan.ai.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillscan.ai.controller.AIAnalysisController;
import com.skillscan.ai.dto.request.AnalysisRequestDTO;
import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.metrics.SkillScanAIMetrics;
import com.skillscan.ai.services.AnalysisOrchestratorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AIAnalysisController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
class AIAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisOrchestratorService orchestrator;

    // Required for GlobalExceptionHandler
    @MockitoBean
    private SkillScanAIMetrics skillScanAIMetrics;

    @Autowired
    private ObjectMapper objectMapper;

    //  Success Case
    @Test
    void shouldAnalyzeSuccessfully() throws Exception {

        AnalysisRequestDTO request = new AnalysisRequestDTO();
        request.setResumeId(UUID.randomUUID());
        request.setJobDescription("Java Developer");

        AIResponse response = AIResponse.builder()
                .finalScore(85.5)
                .ruleScore(80)
                .llmScore(90)
                .skills(List.of("Java", "Spring Boot"))
                .matchedKeywords(List.of("Java", "REST"))
                .missingKeywords(List.of("Docker"))
                .suggestions(List.of("Learn Docker"))
                .build();

        when(orchestrator.analyze(request)).thenReturn(response);

        mockMvc.perform(post("/api/analysis")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalScore").value(85.5))
                .andExpect(jsonPath("$.ruleScore").value(80))
                .andExpect(jsonPath("$.llmScore").value(90))
                .andExpect(jsonPath("$.skills[0]").value("Java"))
                .andExpect(jsonPath("$.matchedKeywords[0]").value("Java"))
                .andExpect(jsonPath("$.missingKeywords[0]").value("Docker"))
                .andExpect(jsonPath("$.suggestions[0]").value("Learn Docker"));
    }

    //  Validation Case (Missing resumeId)
    @Test
    void shouldReturnBadRequest_whenResumeIdMissing() throws Exception {

        AnalysisRequestDTO request = new AnalysisRequestDTO();
        request.setJobDescription("Java Developer");

        mockMvc.perform(post("/api/analysis")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}