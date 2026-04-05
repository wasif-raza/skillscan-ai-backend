package com.skillscan.ai.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ResumeService {

    void uploadResume(UUID userId, MultipartFile file);

    void deleteResumeFile(String filePath);
    // todo implement in feature
//    void deleteResume(UUID resumeId);
}
