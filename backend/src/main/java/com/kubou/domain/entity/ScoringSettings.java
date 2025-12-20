package com.kubou.domain.entity;

public class ScoringSettings {
    private int baseScore;
    private double timeWeight; // 0.0 to 1.0 (0% to 100%)
    private double streakMultiplier; // e.g., 1.1 for 10% bonus per streak

    public ScoringSettings() {
        // Defaults
        this.baseScore = 1000;
        this.timeWeight = 0.5; // 50% importance
        this.streakMultiplier = 1.0; // No streak bonus by default
    }

    public ScoringSettings(int baseScore, double timeWeight, double streakMultiplier) {
        this.baseScore = baseScore;
        this.timeWeight = timeWeight;
        this.streakMultiplier = streakMultiplier;
    }

    public int getBaseScore() {
        return baseScore;
    }

    public void setBaseScore(int baseScore) {
        this.baseScore = baseScore;
    }

    public double getTimeWeight() {
        return timeWeight;
    }

    public void setTimeWeight(double timeWeight) {
        this.timeWeight = timeWeight;
    }

    public double getStreakMultiplier() {
        return streakMultiplier;
    }

    public void setStreakMultiplier(double streakMultiplier) {
        this.streakMultiplier = streakMultiplier;
    }
}
