package com.yomu.core.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_missions", schema = "gamification")
public class UserMission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "mission_id", nullable = false)
    private UUID missionId;

    @Column(nullable = false)
    private Integer progress = 0;

    @Column(nullable = false)
    private Boolean claimed = false;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", insertable = false, updatable = false)
    private DailyMission mission;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getMissionId() { return missionId; }
    public void setMissionId(UUID missionId) { this.missionId = missionId; }
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    public Boolean getClaimed() { return claimed; }
    public void setClaimed(Boolean claimed) { this.claimed = claimed; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public DailyMission getMission() { return mission; }
    public void setMission(DailyMission mission) { this.mission = mission; }
}