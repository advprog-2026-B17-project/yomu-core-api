package com.yomu.core.strategy;

import org.springframework.stereotype.Component;

@Component
public class BronzeScoringStrategy implements ClanScoringStrategy {
    @Override
    public double calculateScore(double totalScore, int memberCount) {
        return totalScore;
    }
}
