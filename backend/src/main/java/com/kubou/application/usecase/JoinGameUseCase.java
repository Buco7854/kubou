package com.kubou.application.usecase;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.domain.entity.GameSession;
import com.kubou.domain.entity.Player;
import org.springframework.stereotype.Service;

@Service
public class JoinGameUseCase {

    private final GameSessionRepository gameSessionRepository;

    public JoinGameUseCase(GameSessionRepository gameSessionRepository) {
        this.gameSessionRepository = gameSessionRepository;
    }

    public void execute(String pin, Player player) {
        GameSession gameSession = gameSessionRepository.findByPin(pin)
                .orElseThrow(() -> new RuntimeException("Game session not found"));

        gameSession.addPlayer(player);
        gameSessionRepository.save(gameSession);
    }
}
