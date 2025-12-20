package com.kubou.domain.entity;

public class GameConfig {
    private ScoringSettings scoringSettings;

    public GameConfig() {
        this.scoringSettings = new ScoringSettings();
    }

    public GameConfig(ScoringSettings scoringSettings) {
        this.scoringSettings = scoringSettings;
    }

    public ScoringSettings getScoringSettings() {
        return scoringSettings;
    }

    public void setScoringSettings(ScoringSettings scoringSettings) {
        this.scoringSettings = scoringSettings;
    }
}
