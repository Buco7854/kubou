package com.kubou.infrastructure.repository.jpa.mapper;

import com.kubou.domain.entity.PlayerAchievement;
import com.kubou.infrastructure.repository.jpa.model.PlayerAchievementData;
import org.springframework.stereotype.Component;

@Component
public class PlayerAchievementMapper {

    public PlayerAchievementData toData(PlayerAchievement achievement) {
        if (achievement == null) return null;
        PlayerAchievementData data = new PlayerAchievementData();
        data.setId(achievement.getId());
        data.setPlayerId(achievement.getPlayerId());
        data.setType(achievement.getType());
        data.setUnlockedAt(achievement.getUnlockedAt());
        return data;
    }

    public PlayerAchievement toDomain(PlayerAchievementData data) {
        if (data == null) return null;
        return new PlayerAchievement(
                data.getId(),
                data.getPlayerId(),
                data.getType(),
                data.getUnlockedAt()
        );
    }
}
