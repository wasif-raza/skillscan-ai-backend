package com.skillscan.ai.controller;

import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.services.AIAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIAnalysisController {

    private final AIAnalysisService aiAnalysisService;

    @PostMapping("/analyze/{resumeId}")
    public ResponseEntity<AIResponse> analyze(@PathVariable UUID resumeId) {
        return ResponseEntity.ok(aiAnalysisService.analyze(resumeId));
    }
}