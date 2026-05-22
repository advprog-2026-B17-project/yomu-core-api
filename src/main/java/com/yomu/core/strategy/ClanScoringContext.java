package com.yomu.core.strategy;

import org.springframework.stereotype.Component;

@Component
public class ClanScoringContext {

    private final BronzeScoringStrategy bronzeStrategy;
    private final SilverGoldScoringStrategy silverGoldStrategy;
    private final DiamondScoringStrategy diamondStrategy;

    public ClanScoringContext(BronzeScoringStrategy bronzeStrategy,
                               SilverGoldScoringStrategy silverGoldStrategy,
                               DiamondScoringStrategy diamondStrategy) {
        this.bronzeStrategy = bronzeStrategy;
        this.silverGoldStrategy = silverGoldStrategy;
        this.diamondStrategy = diamondStrategy;
    }

    public double calculateScore(String tier, double totalScore, int memberCount) {
        ClanScoringStrategy strategy = getStrategy(tier);
        return strategy.calculateScore(totalScore, memberCount);
    }

    private ClanScoringStrategy getStrategy(String tier) {
        return switch (tier.toLowerCase()) {
            case "diamond" -> diamondStrategy;
            case "gold", "silver" -> silverGoldStrategy;
            default -> bronzeStrategy;
        };
    }
}
