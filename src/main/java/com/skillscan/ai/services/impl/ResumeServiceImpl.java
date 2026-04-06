package com.skillscan.ai.services.impl;

import com.skillscan.ai.dto.response.ResumeUploadResponse;
import com.skillscan.ai.exception.BadRequestException;
import com.skillscan.ai.exception.BaseException;
import com.skillscan.ai.exception.UserNotFoundException;
import com.skillscan.ai.mapper.ResumeMapper;
import com.skillscan.ai.model.Resume;
import com.skillscan.ai.model.User;
import com.skillscan.ai.repository.ResumeRepository;
import com.skillscan.ai.repository.UserRepository;
import com.skillscan.ai.services.ResumeParserService;
import com.skillscan.ai.services.ResumeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ResumeParserService parserService;

    private static final Logger log = LoggerFactory.getLogger(ResumeServiceImpl.class);
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.max-size}")
    private DataSize maxFileSize;
    private Path uploadPath;

    @PostConstruct
    public void init() {
        try {
            this.uploadPath = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(uploadPath); //  atomic & cross-platform
        } catch (IOException ex) {
            throw new BaseException("Could not initialize upload directory",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResumeUploadResponse uploadResume(UUID userId, MultipartFile file) {

        //  Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        //  Validate file
        validateFile(file);

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        //  Prevent path traversal
        if (originalFileName.contains("..")) {
            throw new BadRequestException("Invalid file path");
        }

        // Generate unique filename
        String safeFileName= UUID.randomUUID() + ".pdf";

        //  Resolve secure path
        Path targetLocation = uploadPath.resolve(safeFileName).normalize();

        // Check file stays inside upload directory
        if (!targetLocation.startsWith(uploadPath)) {
            throw new BadRequestException("Invalid file path");
        }

        try(InputStream is  = file.getInputStream()) {

            //  Save file
            Files.copy(is, targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String extractedText = "";
            try {
                extractedText = parserService.extractText(targetLocation);

                if (extractedText.isBlank()) {
                    log.info("Resume parsed but content is empty: {}", targetLocation);
                }
                int maxLength = 10000;

                if (extractedText.length() > maxLength) {
                    extractedText = extractedText.substring(0, maxLength);
                    log.info("Resume content truncated to {} characters", maxLength);
                }

            } catch (Exception e) {
                // Parsing should NOT break upload
                log.warn("Parsing failed for file: {}", targetLocation);
            }


            //  Save metadata
            Resume resume = Resume.builder()
                    .user(user)
                    .fileName(originalFileName)
                    .fileType(PDF_CONTENT_TYPE)
                    .filePath(targetLocation.toString())
                    .uploadedAt(LocalDateTime.now())
                    .content(extractedText)
                    .build();

           Resume savedResume = resumeRepository.save(resume);
            return ResumeMapper.toDTO(savedResume, "Resume uploaded successfully");

        } catch (Exception ex) {

            //  Cleanup file if DB save fails
            try {
                Files.deleteIfExists(targetLocation);
            } catch (IOException cleanupEx) {
                log.error("Failed to delete file during cleanup: {}", targetLocation);
            }
            log.error("Resume upload failed for userId: {}", userId, ex);
            throw new BaseException("Failed to upload resume: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // File deletion logic (used by UserService)
    @Override
    public void deleteResumeFile(String filePath) {
        try {
            Path targetPath = Paths.get(filePath).toAbsolutePath().normalize();
            if (!targetPath.startsWith(uploadPath)) {
                throw new BadRequestException("Invalid file path");
            }
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath);
        }
    }

    //  Validation logic
    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }

        //  Size validation (from config)
        if (file.getSize() > maxFileSize.toBytes()) {
            throw new BadRequestException(
                    "File size must be less than " + maxFileSize.toMegabytes() + "MB"
            );
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.trim().isEmpty()) {
            throw new BadRequestException("Invalid file name");
        }

        //  Extension check
        if (!fileName.toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("Only PDF files are allowed");
        }

        //  MIME type check
        if (file.getContentType() == null ||
                !file.getContentType().equalsIgnoreCase(PDF_CONTENT_TYPE)) {
            throw new BadRequestException("Invalid file type");
        }
        //  Real PDF validation
        validatePdfContent(file);
    }
    private void validatePdfContent(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {

            byte[] header = new byte[5];
            int totalRead = 0;

            while (totalRead < 5) {
                int bytesRead = is.read(header, totalRead, 5 - totalRead);

                if (bytesRead == -1) {
                    throw new BadRequestException("Invalid PDF file");
                }

                totalRead += bytesRead;
            }

            String headerStr = new String(header, StandardCharsets.US_ASCII);

            if (!"%PDF-".equals(headerStr)) {
                throw new BadRequestException("Invalid PDF file");
            }

        } catch (IOException e) {
            throw new BadRequestException("Failed to read file");
        }
    }
}