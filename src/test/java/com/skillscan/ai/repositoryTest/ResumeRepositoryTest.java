package com.skillscan.ai.repositoryTest;

import com.skillscan.ai.model.Resume;
import com.skillscan.ai.model.ResumeStatus;
import com.skillscan.ai.model.User;
import com.skillscan.ai.repository.ResumeRepository;
import com.skillscan.ai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private UserRepository userRepository;

    // Test findByUserId
    @Test
    void shouldReturnResumes_whenUserIdExists() {

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .name("Wasif")
                .email("wasif@test.com")
                .build();

        user = userRepository.save(user);

        Resume resume = Resume.builder()
                .user(user)
                .fileName("resume.pdf")
                .fileType("pdf")
                .filePath("/files/resume.pdf")
                .uploadedAt(now)
                .status(ResumeStatus.ACTIVE)
                .expiryTime(now.plusHours(1))
                .build();

        resumeRepository.saveAndFlush(resume);

        List<Resume> result = resumeRepository.findByUserId(user.getId());

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getUser().getId()).isEqualTo(user.getId());
    }

    // Test findExpiredResumes
    @Test
    void shouldReturnExpiredResumes_whenConditionMatches() {

        LocalDateTime now = LocalDateTime.now();

        User user = userRepository.save(
                User.builder()
                        .name("Test")
                        .email("test@test.com")
                        .build()
        );

        Resume expiredResume = Resume.builder()
                .user(user)
                .fileName("old.pdf")
                .fileType("pdf")
                .filePath("/files/old.pdf")
                .uploadedAt(now.minusDays(1))
                .status(ResumeStatus.ACTIVE)
                .expiryTime(now.minusHours(2)) // expired
                .build();

        Resume activeResume = Resume.builder()
                .user(user)
                .fileName("new.pdf")
                .fileType("pdf")
                .filePath("/files/new.pdf")
                .uploadedAt(now)
                .status(ResumeStatus.ACTIVE)
                .expiryTime(now.plusHours(2)) // not expired
                .build();

        resumeRepository.saveAllAndFlush(List.of(expiredResume, activeResume));

        Page<Resume> result = resumeRepository.findExpiredResumes(
                now,
                List.of(ResumeStatus.ACTIVE),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getExpiryTime())
                .isBefore(now);
    }
}