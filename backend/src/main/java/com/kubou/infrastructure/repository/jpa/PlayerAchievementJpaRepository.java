package com.kubou.infrastructure.repository.jpa;

import com.kubou.infrastructure.repository.jpa.model.PlayerAchievementData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlayerAchievementJpaRepository extends JpaRepository<PlayerAchievementData, String> {
    List<PlayerAchievementData> findByPlayerId(String playerId);
}
