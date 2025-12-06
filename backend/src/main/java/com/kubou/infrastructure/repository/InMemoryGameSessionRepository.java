package com.kubou.infrastructure.repository;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.domain.entity.GameSession;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryGameSessionRepository implements GameSessionRepository {

    private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();

    @Override
    public Optional<GameSession> findById(String id) {
        return Optional.ofNullable(sessions.get(id));
    }

    @Override
    public Optional<GameSession> findByPin(String pin) {
        return sessions.values().stream()
                .filter(session -> session.getPin().equals(pin))
                .findFirst();
    }

    @Override
    public void save(GameSession gameSession) {
        sessions.put(gameSession.getId(), gameSession);
    }
}
