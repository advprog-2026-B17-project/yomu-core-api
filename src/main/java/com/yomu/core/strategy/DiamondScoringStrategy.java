package com.yomu.core.strategy;

import org.springframework.stereotype.Component;

@Component
public class DiamondScoringStrategy implements ClanScoringStrategy {
    private static final double ACTIVITY_WEIGHT = 0.6;
    private static final double CONSISTENCY_WEIGHT = 0.4;

    @Override
    public double calculateScore(double totalScore, int memberCount) {
        if (memberCount == 0) return 0;
        double activityScore = totalScore * ACTIVITY_WEIGHT;
        double consistencyScore = (totalScore / memberCount) * CONSISTENCY_WEIGHT;
        return activityScore + consistencyScore;
    }
}
