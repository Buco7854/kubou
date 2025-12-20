package com.kubou.domain.entity;

import java.time.LocalDateTime;

public class PlayerAchievement {
    private String id;
    private String playerId;
    private AchievementType type;
    private LocalDateTime unlockedAt;

    public PlayerAchievement(String id, String playerId, AchievementType type, LocalDateTime unlockedAt) {
        this.id = id;
        this.playerId = playerId;
        this.type = type;
        this.unlockedAt = unlockedAt;
    }

    // Getters
    public String getId() { return id; }
    public String getPlayerId() { return playerId; }
    public AchievementType getType() { return type; }
    public LocalDateTime getUnlockedAt() { return unlockedAt; }
}
