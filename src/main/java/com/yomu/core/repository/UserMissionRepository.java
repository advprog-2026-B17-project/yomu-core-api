package com.yomu.core.repository;

import com.yomu.core.entity.UserMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserMissionRepository extends JpaRepository<UserMission, UUID> {

    List<UserMission> findByUserIdAndDate(UUID userId, LocalDate date);

    List<UserMission> findByUserIdAndDateAndClaimedFalse(UUID userId, LocalDate date);

    Optional<UserMission> findByUserIdAndMissionIdAndDate(UUID userId, UUID missionId, LocalDate date);
}