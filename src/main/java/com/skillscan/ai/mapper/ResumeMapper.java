package com.skillscan.ai.mapper;

import com.skillscan.ai.dto.response.ResumeResponseDTO;
import com.skillscan.ai.model.Resume;

public class ResumeMapper {

    public static ResumeResponseDTO toDTO(Resume resume) {
        return ResumeResponseDTO.builder()
                .id(resume.getId())
                .fileName(resume.getFileName())
                .fileType(resume.getFileType())
                .fileUrl("/uploads/" + resume.getFileName())
                .uploadedAt(resume.getUploadedAt())
                .build();
    }
}