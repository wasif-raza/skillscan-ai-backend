package com.skillscan.ai.services;

import com.skillscan.ai.dto.request.AnalysisRequestDTO;
import com.skillscan.ai.dto.response.AIResponse;

public interface AnalysisOrchestratorService {


    public AIResponse analyze(
            AnalysisRequestDTO request,
            boolean guest
    );
}
