package com.kubou.application.repository;

import com.kubou.domain.entity.GameSession;
import java.util.Optional;

public interface GameSessionRepository {
    Optional<GameSession> findById(String id);
    Optional<GameSession> findByPin(String pin);
    void save(GameSession gameSession);
}
