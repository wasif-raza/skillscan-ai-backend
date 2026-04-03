package com.skillscan.ai.services.impl;


import com.skillscan.ai.exception.BadRequestException;
import com.skillscan.ai.exception.BaseException;
import com.skillscan.ai.exception.UserNotFoundException;
import com.skillscan.ai.model.Resume;
import com.skillscan.ai.model.User;
import com.skillscan.ai.repository.ResumeRepository;
import com.skillscan.ai.repository.UserRepository;
import com.skillscan.ai.services.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    //  Use stable path
    private static final String UPLOAD_DIR = "C:/temp/uploads/";

    @Override
    public void uploadResume(UUID userId, MultipartFile file) {

        //  Validate User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        //  Validate File
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new BadRequestException("Invalid file name");
        }

        //  Validate PDF (extension-based, more reliable)
        if (!originalFileName.toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("Only PDF files are allowed");
        }

        try {
            // Create directory if not exists
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    throw new BaseException("Failed to create upload directory", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            //  Generate unique file name
            String uniqueFileName = UUID.randomUUID() + "_" + originalFileName;

            String filePath = UPLOAD_DIR + uniqueFileName;


            // Save file
            File destinationFile = new File(filePath);
            file.transferTo(destinationFile);

            //  Save metadata to DB
            Resume resume = Resume.builder()
                    .id(UUID.randomUUID())
                    .user(user)
                    .fileName(originalFileName)
                    .fileType(file.getContentType())
                    .filePath(filePath)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            resumeRepository.save(resume);

        } catch (IOException e) {
            throw new BaseException("Failed to upload resume", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}