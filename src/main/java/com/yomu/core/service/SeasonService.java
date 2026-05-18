package com.yomu.core.service;

import com.yomu.core.dto.CreateSeasonRequest;
import com.yomu.core.dto.SeasonDTO;
import com.yomu.core.entity.Clan;
import com.yomu.core.entity.Season;
import com.yomu.core.repository.ClanRepository;
import com.yomu.core.repository.SeasonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class SeasonService {

    private final SeasonRepository seasonRepository;
    private final ClanRepository clanRepository;
    private final EventPublisher eventPublisher;

    public SeasonService(SeasonRepository seasonRepository, ClanRepository clanRepository, EventPublisher eventPublisher) {
        this.seasonRepository = seasonRepository;
        this.clanRepository = clanRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<SeasonDTO> getAllSeasons() {
        return seasonRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public Optional<SeasonDTO> getActiveSeason() {
        return seasonRepository.findByIsActiveTrue().map(this::toDTO);
    }

    @Transactional
    public SeasonDTO createSeason(CreateSeasonRequest request) {
        // Deactivate any currently active season
        seasonRepository.findByIsActiveTrue().ifPresent(currentSeason -> {
            currentSeason.setActive(false);
            currentSeason.setEndDate(OffsetDateTime.now());
            seasonRepository.save(currentSeason);
        });

        Season season = new Season();
        season.setName(request.getName());
        season.setStartDate(request.getStartDate() != null ? request.getStartDate() : OffsetDateTime.now());
        if (request.getEndDate() != null) {
            season.setEndDate(request.getEndDate());
        }
        season.setActive(true);
        season = seasonRepository.save(season);

        return toDTO(season);
    }

    @Transactional
    public Optional<SeasonDTO> endSeason(UUID seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new RuntimeException("Season not found"));

        season.setActive(false);
        season.setEndDate(OffsetDateTime.now());
        season = seasonRepository.save(season);

        // Calculate final rankings and tier changes
        List<Map<String, Object>> rankings = calculateFinalRankings();

        // Auto-create new season
        Season newSeason = new Season();
        newSeason.setName(generateNextSeasonName());
        newSeason.setStartDate(OffsetDateTime.now());
        newSeason.setActive(true);
        seasonRepository.save(newSeason);

        // Publish season.ended event
        eventPublisher.publishSeasonEnded(seasonId, rankings);

        return Optional.of(toDTO(season));
    }

    private List<Map<String, Object>> calculateFinalRankings() {
        List<Clan> clans = clanRepository.findAllOrderByTotalScoreDesc();
        List<Map<String, Object>> rankings = new ArrayList<>();

        for (int i = 0; i < clans.size(); i++) {
            Clan clan = clans.get(i);
            String newTier = calculateTier(i + 1, clans.size());

            Map<String, Object> entry = new HashMap<>();
            entry.put("clanId", clan.getId().toString());
            entry.put("clanName", clan.getName());
            entry.put("totalScore", clan.getTotalScore().doubleValue());
            entry.put("newTier", newTier);
            rankings.add(entry);

            // Update clan tier immediately
            clan.setTier(newTier);
            clanRepository.save(clan);
        }

        return rankings;
    }

    private String calculateTier(int rank, int totalClans) {
        if (totalClans == 0) return "bronze";

        double percentile = (double) rank / totalClans;

        if (percentile <= 0.1) {
            return "diamond";
        } else if (percentile <= 0.3) {
            return "gold";
        } else if (percentile <= 0.6) {
            return "silver";
        } else {
            return "bronze";
        }
    }

    private String generateNextSeasonName() {
        long seasonCount = seasonRepository.count();
        return "Season " + (seasonCount + 1);
    }

    private SeasonDTO toDTO(Season season) {
        return new SeasonDTO(
            season.getId(),
            season.getName(),
            season.getStartDate(),
            season.getEndDate(),
            season.isActive()
        );
    }
}