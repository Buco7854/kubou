package com.kubou.application.usecase;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.domain.entity.GameSession;
import com.kubou.domain.entity.Player;
import com.kubou.infrastructure.repository.InMemoryGameSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JoinGameUseCaseTest {

    private JoinGameUseCase joinGameUseCase;
    private GameSessionRepository gameSessionRepository;
    private GameSession existingSession;

    @BeforeEach
    void setUp() {
        gameSessionRepository = new InMemoryGameSessionRepository();
        joinGameUseCase = new JoinGameUseCase(gameSessionRepository);

        // Create a pre-existing game session for players to join
        existingSession = new GameSession("game-1", "123456", "quiz-1", "host-1");
        gameSessionRepository.save(existingSession);
    }

    @Test
    void shouldAddPlayerToExistingGameSession() {
        // Given
        Player newPlayer = new Player("player-1", "John");
        String gamePin = "123456";

        // When
        joinGameUseCase.execute(gamePin, newPlayer);

        // Then
        GameSession updatedSession = gameSessionRepository.findByPin(gamePin).orElseThrow();
        assertEquals(1, updatedSession.getPlayers().size());
        assertEquals("player-1", updatedSession.getPlayers().get(0).getId());
        assertEquals("John", updatedSession.getPlayers().get(0).getNickname());
    }

    @Test
    void shouldThrowExceptionWhenJoiningNonExistentGame() {
        // Given
        Player newPlayer = new Player("player-2", "Jane");
        String invalidPin = "999999";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            joinGameUseCase.execute(invalidPin, newPlayer);
        }, "Game session not found");
    }
}
