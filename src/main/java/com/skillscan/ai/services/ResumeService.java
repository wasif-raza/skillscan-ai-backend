package com.skillscan.ai.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ResumeService {

    void uploadResume(UUID userId, MultipartFile file);

}