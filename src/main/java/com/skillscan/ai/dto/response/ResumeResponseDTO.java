package com.skillscan.ai.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ResumeResponseDTO {

    private UUID id;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private LocalDateTime uploadedAt;
}
