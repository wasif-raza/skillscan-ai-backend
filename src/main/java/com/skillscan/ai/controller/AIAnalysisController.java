package com.skillscan.ai.controller;

import com.skillscan.ai.dto.request.AnalysisRequestDTO;
import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.services.AnalysisOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AIAnalysisController {

    private final AnalysisOrchestratorService orchestrator;

    @PostMapping
    public AIResponse analyze(@RequestBody AnalysisRequestDTO request) {
        return orchestrator.analyze(request);
    }
}