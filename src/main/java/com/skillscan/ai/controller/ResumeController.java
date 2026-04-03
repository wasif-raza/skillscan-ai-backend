package com.skillscan.ai.controller;


import com.skillscan.ai.services.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.UUID;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    //  Upload Resume
    @PostMapping("/upload")
    public ResponseEntity<String> uploadResume(
            @RequestParam UUID userId,
            @RequestParam MultipartFile file
    ) {
        resumeService.uploadResume(userId, file);
        return ResponseEntity.ok("Resume uploaded successfully");
    }

}