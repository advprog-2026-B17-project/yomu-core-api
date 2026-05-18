package com.yomu.core.repository;

import com.yomu.core.entity.ClanMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClanMemberRepository extends JpaRepository<ClanMember, UUID> {
    List<ClanMember> findByClanId(UUID clanId);
    List<ClanMember> findByUserId(UUID userId);
    Optional<ClanMember> findByClanIdAndUserId(UUID clanId, UUID userId);
    boolean existsByClanIdAndUserId(UUID clanId, UUID userId);
    void deleteByClanIdAndUserId(UUID clanId, UUID userId);
}