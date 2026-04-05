package com.skillscan.ai.model;

import jakarta.persistence.*;
import lombok.*;

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
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String fileName;
    private String fileType;
    private  String filePath;

    private LocalDateTime uploadedAt;

}
