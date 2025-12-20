package com.kubou.infrastructure.repository;

import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.domain.entity.PlayerResponse;
import com.kubou.infrastructure.repository.jpa.PlayerResponseJpaRepository;
import com.kubou.infrastructure.repository.jpa.mapper.PlayerResponseMapper;
import com.kubou.infrastructure.repository.jpa.model.PlayerResponseData;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlayerResponseRepositoryImpl implements PlayerResponseRepository {

    private final PlayerResponseJpaRepository jpaRepository;
    private final PlayerResponseMapper mapper;

    public PlayerResponseRepositoryImpl(PlayerResponseJpaRepository jpaRepository, PlayerResponseMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(PlayerResponse response) {
        PlayerResponseData data = mapper.toData(response);
        jpaRepository.save(data);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerResponse> findByGameSessionId(String gameSessionId) {
        return jpaRepository.findByGameSessionId(gameSessionId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerResponse> findByPlayerId(String playerId) {
        return jpaRepository.findByPlayerId(playerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerResponse> findByPlayerIdAndDateRange(String playerId, LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByPlayerIdAndTimestampBetween(playerId, start, end).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
