package com.skillscan.ai.controller;


import com.skillscan.ai.dto.response.ResumeUploadResponse;
import com.skillscan.ai.services.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    @PostMapping
    public ResponseEntity<ResumeUploadResponse> uploadResume(
            @RequestParam("file") MultipartFile file
    ) {
        ResumeUploadResponse response = resumeService.uploadResume( file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
    //  todo Resume (Single)
//    @DeleteMapping("/{resumeId}")
//    public ResponseEntity<Void> deleteResume(@PathVariable UUID resumeId) {
//        resumeService.deleteResume(resumeId);
//        return ResponseEntity.noContent().build();
//    }
}