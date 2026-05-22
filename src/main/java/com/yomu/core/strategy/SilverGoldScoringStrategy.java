package com.yomu.core.strategy;

import org.springframework.stereotype.Component;

@Component
public class SilverGoldScoringStrategy implements ClanScoringStrategy {
    @Override
    public double calculateScore(double totalScore, int memberCount) {
        if (memberCount == 0) return 0;
        return totalScore / memberCount;
    }
}
