package com.skillscan.ai.controllerTest;

import com.skillscan.ai.controller.ResumeController;
import com.skillscan.ai.dto.response.ResumeUploadResponse;
import com.skillscan.ai.metrics.SkillScanAIMetrics;
import com.skillscan.ai.services.ResumeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ResumeController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResumeService resumeService;

    //  Required because GlobalExceptionHandler depends on it
    @MockitoBean
    private SkillScanAIMetrics skillScanAIMetrics;

    //  Success Case
    @Test
    void shouldUploadResume() throws Exception {

        UUID userId = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "dummy content".getBytes()
        );

        ResumeUploadResponse response = ResumeUploadResponse.builder()
                .resumeId(UUID.randomUUID())
                .fileName("resume.pdf")
                .message("Uploaded successfully")
                .build();

        when(resumeService.uploadResume(any(), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/resumes/{userId}", userId)
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("resume.pdf"))
                .andExpect(jsonPath("$.message").value("Uploaded successfully"));
    }

    //  Failure Case (Missing File)
    @Test
    void shouldReturnBadRequest_whenFileMissing() throws Exception {

        UUID userId = UUID.randomUUID();

        mockMvc.perform(multipart("/api/resumes/{userId}", userId))
                .andExpect(status().isBadRequest());
    }
}