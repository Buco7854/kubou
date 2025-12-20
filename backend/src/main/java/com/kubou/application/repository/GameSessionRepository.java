package com.kubou.application.repository;

import com.kubou.domain.entity.GameSession;
import java.util.List;
import java.util.Optional;

public interface GameSessionRepository {
    Optional<GameSession> findById(String id);
    Optional<GameSession> findByPin(String pin);
    List<GameSession> findByHostId(String hostId);
    void save(GameSession gameSession);
}
