package com.skillscan.ai.unitTest;

import com.skillscan.ai.model.Resume;
import com.skillscan.ai.model.ResumeStatus;
import com.skillscan.ai.repository.ResumeRepository;
import com.skillscan.ai.services.impl.ResumeCleanupServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeCleanupServiceImplTest {

    @Mock
    private ResumeRepository resumeRepository;

    @InjectMocks
    private ResumeCleanupServiceImpl service;

    @BeforeEach
    void setUp() {
        // Inject self manually (important for transactional proxy simulation)
        ReflectionTestUtils.setField(service, "self", service);
    }

    //  SUCCESS CASE
    @Test
    void handleDeletion_shouldMarkDeleted_whenFileExists() throws Exception {
        Resume resume = new Resume();
        resume.setId(UUID.randomUUID());
        resume.setRetryCount(0);

        Path tempFile = Files.createTempFile("resume", ".txt");
        resume.setFilePath(tempFile.toString());

        service.handleDeletion(resume);

        assertEquals(ResumeStatus.DELETED, resume.getStatus());
        verify(resumeRepository).save(resume);
    }

    //  RETRY CASE
    @Test
    void handleDeletion_shouldRetry_whenDeletionFails() {
        Resume resume = new Resume();
        resume.setId(UUID.randomUUID());
        resume.setFilePath(null); // triggers exception
        resume.setRetryCount(0);

        service.handleDeletion(resume);

        assertEquals(1, resume.getRetryCount());
        assertEquals(ResumeStatus.FAILED, resume.getStatus());
        verify(resumeRepository).save(resume);
    }

    //  PERMANENT FAILURE
    @Test
    void handleDeletion_shouldMarkPermanentFailure_whenMaxRetriesReached() {
        Resume resume = new Resume();
        resume.setId(UUID.randomUUID());
        resume.setFilePath(null);
        resume.setRetryCount(2); // MAX_RETRIES = 3

        service.handleDeletion(resume);

        assertEquals(3, resume.getRetryCount());
        assertEquals(ResumeStatus.PERMANENTLY_FAILED, resume.getStatus());
        verify(resumeRepository).save(resume);
    }

    //  PROCESS BATCH
    @Test
    void processExpiredResumes_shouldCallHandleDeletion() {
        Resume resume = new Resume();
        resume.setId(UUID.randomUUID());
        resume.setFilePath(null);

        Page<Resume> page = new PageImpl<>(List.of(resume));

        when(resumeRepository.findExpiredResumes(
                any(),
                any(),
                any(Pageable.class)
        )).thenReturn(page);

        ResumeCleanupServiceImpl spyService = Mockito.spy(service);
        ReflectionTestUtils.setField(spyService, "self", spyService);

        spyService.processExpiredResumes();

        verify(spyService).handleDeletion(resume);
    }

    //  DELETE FILE - INVALID PATH
    @Test
    void deleteFile_shouldThrowException_whenPathInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteFile("::invalid-path::"));
    }

    //  DELETE FILE - FILE NOT FOUND (no exception expected)
    @Test
    void deleteFile_shouldNotThrow_whenFileDoesNotExist() throws IOException {
        service.deleteFile("non-existing-file.txt");
        // no exception = success
    }
}