package com.skillscan.ai.services.impl;

import com.skillscan.ai.exception.FileDeletionException;
import com.skillscan.ai.model.Resume;
import com.skillscan.ai.model.ResumeStatus;
import com.skillscan.ai.repository.ResumeRepository;
import com.skillscan.ai.services.ResumeCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeCleanupServiceImpl implements ResumeCleanupService {

    private final ResumeRepository resumeRepository;

    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 3;


    @Override
    @Transactional
    public void processExpiredResumes() {

        int page = 0;
        Page<Resume> result;

        do {
             result = resumeRepository.findExpiredResumes(
                    LocalDateTime.now(),
                     List.of(ResumeStatus.ACTIVE, ResumeStatus.FAILED),
                    PageRequest.of(page, BATCH_SIZE)
            );

            for (Resume resume : result.getContent()) {
                handleDeletion(resume);
            }

            page++;

        } while (result.hasNext());
    }

    private void handleDeletion(Resume resume) {
        try {
            deleteFile(resume.getFilePath());

            resume.setStatus(ResumeStatus.DELETED);

            log.info("Deleted resume ID={}", resume.getId());

        } catch (Exception ex) {
            int retries = resume.getRetryCount() + 1;
            resume.setRetryCount(retries);

            if(retries >= MAX_RETRIES){
                resume.setStatus(ResumeStatus.PERMANENTLY_FAILED);
                log.error("Permanent failure for resume ID={}", resume.getId());
            }else {
                resume.setStatus(ResumeStatus.FAILED);
            }

            throw new FileDeletionException("Failed deleting resume ID:"+ resume.getId(), ex);
        }

        resumeRepository.save(resume);
    }

    private void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.deleteIfExists(path);
    }
}