package com.kubou.application.usecase;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.domain.entity.GameSession;
import com.kubou.domain.entity.GameState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StartGameUseCase {

    private final GameSessionRepository gameSessionRepository;

    public StartGameUseCase(GameSessionRepository gameSessionRepository) {
        this.gameSessionRepository = gameSessionRepository;
    }

    @Transactional
    public GameSession execute(String gameId, String userId) {
        GameSession session = gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        if (!session.getHostId().equals(userId)) {
            throw new SecurityException("Only the host can start the game.");
        }

        if (session.getState() != GameState.LOBBY) {
            throw new IllegalStateException("Game has already started or finished.");
        }

        session.setState(GameState.IN_PROGRESS);
        session.setCurrentQuestionIndex(0); // Set to the first question
        gameSessionRepository.save(session);
        return session;
    }
}
