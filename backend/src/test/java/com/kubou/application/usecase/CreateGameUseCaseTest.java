package com.kubou.application.usecase;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.domain.entity.GameSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateGameUseCaseTest {

    private CreateGameUseCase createGameUseCase;

    @Mock
    private GameSessionRepository gameSessionRepository;

    @BeforeEach
    void setUp() {
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
        verify(gameSessionRepository).save(any(GameSession.class));
    }
}
