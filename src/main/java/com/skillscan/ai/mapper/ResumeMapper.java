package com.skillscan.ai.mapper;

import com.skillscan.ai.dto.response.ResumeUploadResponse;
import com.skillscan.ai.model.Resume;

public class ResumeMapper {

    // Prevent instantiation
    private ResumeMapper() {}

    public static ResumeUploadResponse toDTO(Resume resume, String message) {
        return ResumeUploadResponse.builder()
                .resumeId(resume.getId())
                .fileName(resume.getFileName())
                .message(message)
                .build();
    }
}