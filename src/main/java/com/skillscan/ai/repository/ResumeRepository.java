package com.skillscan.ai.repository;

import com.skillscan.ai.model.Resume;
import com.skillscan.ai.model.ResumeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {

    List<Resume> findByUserId(UUID userId);

    @Query("""
    SELECT r FROM Resume r
    WHERE r.expiryTime < :now
    AND r.status IN :statuses
""")
    Page<Resume> findExpiredResumes(
           @Param("now") LocalDateTime now,
           @Param("statuses") List<ResumeStatus> statuses,
            Pageable pageable
    );

}