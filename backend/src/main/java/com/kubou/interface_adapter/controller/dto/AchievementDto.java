package com.kubou.interface_adapter.controller.dto;

import com.kubou.domain.entity.AchievementType;
import java.time.LocalDateTime;

public class AchievementDto {
    private AchievementType type;
    private String name;
    private String description;
    private boolean unlocked;
    private LocalDateTime unlockedAt;

    public AchievementDto(AchievementType type, String name, String description, boolean unlocked, LocalDateTime unlockedAt) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.unlocked = unlocked;
        this.unlockedAt = unlockedAt;
    }

    public AchievementType getType() { return type; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isUnlocked() { return unlocked; }
    public LocalDateTime getUnlockedAt() { return unlockedAt; }
}
