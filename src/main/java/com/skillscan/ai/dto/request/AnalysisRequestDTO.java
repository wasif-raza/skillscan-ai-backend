package com.skillscan.ai.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AnalysisRequestDTO {

    @NotNull
    private UUID resumeId;        // optional (DB source)
    private String jobDescription; // optional
}