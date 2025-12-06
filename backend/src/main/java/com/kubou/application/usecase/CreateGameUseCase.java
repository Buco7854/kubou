package com.kubou.application.usecase;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.domain.entity.GameSession;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CreateGameUseCase {

    private final GameSessionRepository gameSessionRepository;

    public CreateGameUseCase(GameSessionRepository gameSessionRepository) {
        this.gameSessionRepository = gameSessionRepository;
    }

    public GameSession execute(String quizId, String hostId) {
        String gameId = UUID.randomUUID().toString();
        String pin = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        GameSession gameSession = new GameSession(gameId, pin, quizId, hostId);
        gameSessionRepository.save(gameSession);
        return gameSession;
    }
}
