package com.skillscan.ai.unitTest;

import com.skillscan.ai.exception.UserNotFoundException;
import com.skillscan.ai.model.User;
import com.skillscan.ai.repository.ResumeRepository;
import com.skillscan.ai.repository.UserRepository;
import com.skillscan.ai.services.ResumeParserService;
import com.skillscan.ai.services.impl.ResumeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ResumeServiceImplTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResumeParserService parserService;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    private UUID userId;

    @BeforeEach
    void setup() throws Exception {
        userId = UUID.randomUUID();

        // set config manually
        resumeService = new ResumeServiceImpl(resumeRepository, userRepository, parserService);

        // inject values manually (since @Value not loaded)
        java.lang.reflect.Field uploadDir = ResumeServiceImpl.class.getDeclaredField("uploadDir");
        uploadDir.setAccessible(true);
        uploadDir.set(resumeService, "test-uploads");

        java.lang.reflect.Field maxSize = ResumeServiceImpl.class.getDeclaredField("maxFileSize");
        maxSize.setAccessible(true);
        maxSize.set(resumeService, DataSize.ofMegabytes(5));

        resumeService.init();
    }

    //  SUCCESS CASE
    @Test
    void shouldUploadResumeSuccessfully() throws Exception {

        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "%PDF-test content".getBytes()
        );

        when(parserService.extractText(any())).thenReturn("Sample resume content");

        when(resumeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = resumeService.uploadResume(userId, file);

        assertNotNull(response);
        verify(resumeRepository, times(1)).save(any());
    }

    //  USER NOT FOUND
    @Test
    void shouldThrowIfUserNotFound() {

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "%PDF-test".getBytes()
        );

        assertThrows(UserNotFoundException.class, () ->
                resumeService.uploadResume(userId, file)
        );
    }

    //  EMPTY FILE
    @Test
    void shouldThrowIfFileEmpty() {

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(RuntimeException.class, () ->
                resumeService.uploadResume(userId, file)
        );
    }

    //  WRONG FILE TYPE
    @Test
    void shouldThrowIfNotPdf() {

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.txt",
                "text/plain",
                "hello".getBytes()
        );

        assertThrows(RuntimeException.class, () ->
                resumeService.uploadResume(userId, file)
        );
    }

    // ⚠ PARSER FAILS BUT SHOULD NOT BREAK
    @Test
    void shouldContinueIfParserFails() throws Exception {

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "%PDF-test content".getBytes()
        );

        when(parserService.extractText(any())).thenThrow(new RuntimeException("Parser failed"));

        when(resumeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = resumeService.uploadResume(userId, file);

        assertNotNull(response);
        verify(resumeRepository).save(any());
    }
}