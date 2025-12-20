package com.kubou.application.repository;

import com.kubou.domain.entity.PlayerAchievement;
import java.util.List;

public interface PlayerAchievementRepository {
    void save(PlayerAchievement achievement);
    List<PlayerAchievement> findByPlayerId(String playerId);
}
