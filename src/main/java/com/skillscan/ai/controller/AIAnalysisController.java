package com.skillscan.ai.controller;

import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.services.AIAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AIAnalysisController {

    private final AIAnalysisService aiAnalysisService;

    @GetMapping("/resumes/{resumeId}/analysis")
    public ResponseEntity<AIResponse> analyze(@PathVariable UUID resumeId) {

        log.info("Analyzing resumeId={}", resumeId);

        AIResponse response = aiAnalysisService.analyze(resumeId);
        return ResponseEntity.ok(response);
    }
}