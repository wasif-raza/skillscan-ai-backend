package com.skillscan.ai.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class AnalysisRequestDTO {

    private UUID resumeId;        // optional (DB source)
    private String jobDescription; // optional
}