package com.kubou.infrastructure.repository;

import com.kubou.application.repository.PlayerAchievementRepository;
import com.kubou.domain.entity.PlayerAchievement;
import com.kubou.infrastructure.repository.jpa.PlayerAchievementJpaRepository;
import com.kubou.infrastructure.repository.jpa.mapper.PlayerAchievementMapper;
import com.kubou.infrastructure.repository.jpa.model.PlayerAchievementData;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlayerAchievementRepositoryImpl implements PlayerAchievementRepository {

    private final PlayerAchievementJpaRepository jpaRepository;
    private final PlayerAchievementMapper mapper;

    public PlayerAchievementRepositoryImpl(PlayerAchievementJpaRepository jpaRepository, PlayerAchievementMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(PlayerAchievement achievement) {
        PlayerAchievementData data = mapper.toData(achievement);
        jpaRepository.save(data);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerAchievement> findByPlayerId(String playerId) {
        return jpaRepository.findByPlayerId(playerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
