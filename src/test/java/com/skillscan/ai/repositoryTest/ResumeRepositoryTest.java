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

    //  Test findByUserId
    @Test
    void shouldReturnResumes_whenUserIdExists() {

        User user = User.builder()
                .name("Wasif")
                .email("wasif@test.com")
                .build();

        user = userRepository.save(user); // important

        Resume resume = Resume.builder()
                .user(user) //  correct mapping
                .fileName("resume.pdf")
                .fileType("pdf")
                .filePath("/files/resume.pdf")
                .uploadedAt(LocalDateTime.now())
                .status(ResumeStatus.ACTIVE)
                .expiryTime(LocalDateTime.now().plusHours(1))
                .build();

        resumeRepository.save(resume);

        List<Resume> result = resumeRepository.findByUserId(user.getId());

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getUser().getId()).isEqualTo(user.getId());
    }

    // Test findExpiredResumes
    @Test
    void shouldReturnExpiredResumes_whenConditionMatches() {

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
                .uploadedAt(LocalDateTime.now().minusDays(1))
                .status(ResumeStatus.ACTIVE)
                .expiryTime(LocalDateTime.now().minusHours(2)) // expired
                .build();

        Resume activeResume = Resume.builder()
                .user(user)
                .fileName("new.pdf")
                .fileType("pdf")
                .filePath("/files/new.pdf")
                .uploadedAt(LocalDateTime.now())
                .status(ResumeStatus.ACTIVE)
                .expiryTime(LocalDateTime.now().plusHours(2)) // not expired
                .build();

        resumeRepository.saveAll(List.of(expiredResume, activeResume));

        Page<Resume> result = resumeRepository.findExpiredResumes(
                LocalDateTime.now(),
                List.of(ResumeStatus.ACTIVE),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getExpiryTime())
                .isBefore(LocalDateTime.now());
    }
}