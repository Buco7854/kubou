package com.kubou.application.usecase;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.domain.entity.GameSession;
import com.kubou.infrastructure.repository.InMemoryGameSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateGameUseCaseTest {

    private CreateGameUseCase createGameUseCase;
    private GameSessionRepository gameSessionRepository;

    @BeforeEach
    void setUp() {
        // Use the in-memory implementation for fast, isolated testing
        gameSessionRepository = new InMemoryGameSessionRepository();
        createGameUseCase = new CreateGameUseCase(gameSessionRepository);
    }

    @Test
    void shouldCreateAndSaveGameSessionWithUniquePin() {
        // Given
        String quizId = "quiz-123";
        String hostId = "host-abc";

        // When
        GameSession createdSession = createGameUseCase.execute(quizId, hostId);

        // Then
        assertNotNull(createdSession);
        assertNotNull(createdSession.getId());
        assertNotNull(createdSession.getPin());
        assertEquals(6, createdSession.getPin().length());
        assertEquals(quizId, createdSession.getQuizId());
        assertEquals(hostId, createdSession.getHostId());

        // Verify it was saved
        GameSession savedSession = gameSessionRepository.findById(createdSession.getId()).orElse(null);
        assertNotNull(savedSession);
        assertEquals(createdSession.getPin(), savedSession.getPin());
    }
}
