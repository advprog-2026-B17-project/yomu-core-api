package com.yomu.core.repository;

import com.yomu.core.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    List<QuizAttempt> findByUserId(UUID userId);
    List<QuizAttempt> findByReadingId(UUID readingId);
    long countByUserId(UUID userId);
    boolean existsByUserIdAndReadingId(UUID userId, UUID readingId);

    @Query("SELECT AVG(q.accuracy) FROM QuizAttempt q WHERE q.userId = :userId")
    Double getAverageAccuracyByUserId(@Param("userId") UUID userId);
}