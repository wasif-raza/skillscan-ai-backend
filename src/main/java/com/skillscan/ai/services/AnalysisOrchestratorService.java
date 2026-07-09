package com.skillscan.ai.services;

import com.skillscan.ai.dto.request.AnalysisRequestDTO;
import com.skillscan.ai.dto.response.AIResponse;

public interface AnalysisOrchestratorService {

     AIResponse analyze(AnalysisRequestDTO request);
}
