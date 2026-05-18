package com.yomu.core.repository;

import com.yomu.core.entity.CompletedReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompletedReadingRepository extends JpaRepository<CompletedReading, UUID> {
    boolean existsByUserIdAndReadingId(UUID userId, UUID readingId);
    long countByUserId(UUID userId);
}
