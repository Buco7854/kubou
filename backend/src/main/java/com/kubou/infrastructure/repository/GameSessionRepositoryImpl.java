package com.kubou.infrastructure.repository;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.domain.entity.GameSession;
import com.kubou.infrastructure.repository.jpa.GameSessionJpaRepository;
import com.kubou.infrastructure.repository.jpa.mapper.GameSessionMapper;
import com.kubou.infrastructure.repository.jpa.model.GameSessionData;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component("jpaGameSessionRepository")
public class GameSessionRepositoryImpl implements GameSessionRepository {

    private final GameSessionJpaRepository gameSessionJpaRepository;
    private final GameSessionMapper gameSessionMapper;

    public GameSessionRepositoryImpl(GameSessionJpaRepository gameSessionJpaRepository, GameSessionMapper gameSessionMapper) {
        this.gameSessionJpaRepository = gameSessionJpaRepository;
        this.gameSessionMapper = gameSessionMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GameSession> findById(String id) {
        return gameSessionJpaRepository.findById(id).map(gameSessionMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GameSession> findByPin(String pin) {
        return gameSessionJpaRepository.findByPin(pin).map(gameSessionMapper::toDomain);
    }

    @Override
    @Transactional
    public void save(GameSession gameSession) {
        GameSessionData gameSessionData = gameSessionMapper.toData(gameSession);
        gameSessionJpaRepository.save(gameSessionData);
    }
}
