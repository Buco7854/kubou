package com.kubou.infrastructure.repository.jpa.model;

import com.kubou.domain.entity.AchievementType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_achievements")
public class PlayerAchievementData {
    @Id
    private String id;
    private String playerId;
    
    @Enumerated(EnumType.STRING)
    private AchievementType type;

    private LocalDateTime unlockedAt;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public AchievementType getType() { return type; }
    public void setType(AchievementType type) { this.type = type; }
    public LocalDateTime getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }
}
