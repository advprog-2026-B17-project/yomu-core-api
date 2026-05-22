package com.yomu.core.repository;

import com.yomu.core.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("SELECT e FROM OutboxEvent e WHERE e.publishedAt IS NULL AND e.publishAttempts < 5 ORDER BY e.createdAt ASC")
    List<OutboxEvent> findUnpublished();

    @Modifying
    @Query("UPDATE OutboxEvent e SET e.publishedAt = :publishedAt WHERE e.id = :id")
    void markPublished(@Param("id") UUID id, @Param("publishedAt") OffsetDateTime publishedAt);

    @Modifying
    @Query("UPDATE OutboxEvent e SET e.publishAttempts = e.publishAttempts + 1, e.lastError = :error WHERE e.id = :id")
    void incrementAttempts(@Param("id") UUID id, @Param("error") String error);
}