package com.skillscan.ai.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "resumes")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    @Enumerated(EnumType.STRING)
    private ResumeStatus status;

    @Column(name = "retry_count")
    private int retryCount = 0;
}