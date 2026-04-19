package com.skillscan.ai.scheduler;

import com.skillscan.ai.services.ResumeCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeCleanupScheduler {
    private final ResumeCleanupService cleanupService;

    @Scheduled(cron = "0 0 * * * *") // every hour
    public void runCleanupJob() {
        log.info("Starting Resume Cleanup Job");
        try{
            cleanupService.processExpiredResumes();
        } catch (Exception e) {
            log.error("Cleanup job failed", e);
        }
    }
}
