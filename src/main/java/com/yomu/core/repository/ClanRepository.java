package com.yomu.core.repository;

import com.yomu.core.entity.Clan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClanRepository extends JpaRepository<Clan, UUID> {
    Optional<Clan> findByName(String name);
    boolean existsByName(String name);

    @Query(value = "SELECT * FROM gamification.clans ORDER BY total_score DESC", nativeQuery = true)
    List<Clan> findAllOrderByTotalScoreDesc();

    @Query(value = "SELECT * FROM gamification.clans WHERE tier = :tier ORDER BY total_score DESC", nativeQuery = true)
    List<Clan> findByTierOrderByTotalScoreDesc(@Param("tier") String tier);
}