package com.skillscan.ai.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ResumeUploadResponse {
    private UUID resumeId;
    private String fileName;
    private String message;
}