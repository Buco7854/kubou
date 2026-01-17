package com.kubou.application.usecase;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.application.service.AchievementService;
import com.kubou.domain.entity.*;
import com.kubou.domain.service.IScoringStrategy;
import com.kubou.domain.service.SimpleScoringStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmitAnswerUseCaseTest {

    private SubmitAnswerUseCase submitAnswerUseCase;

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private PlayerResponseRepository playerResponseRepository;

    @Mock
    private AchievementService achievementService;

    private IScoringStrategy scoringStrategy;

    private GameSession session;
    private Player player;
    private Quiz quiz;
    private Question question;

    @BeforeEach
    void setUp() {
        scoringStrategy = new SimpleScoringStrategy(); // Using the simple strategy for this test

        submitAnswerUseCase = new SubmitAnswerUseCase(
                gameSessionRepository, 
                quizRepository, 
                playerResponseRepository, 
                scoringStrategy, 
                achievementService
        );

        // Setup test data
        player = new Player("player-1", "TestPlayer");
        question = new Question("q1", "2+2?", Arrays.asList("3", "4", "5"), 1, Collections.emptyList(), 1);
        quiz = new Quiz("quiz-1", "Math Quiz", Collections.singletonList(question));
        session = new GameSession("game-1", "123456", "quiz-1", "host-1");
        session.setState(GameState.IN_PROGRESS);
        session.addPlayer(player);
    }

    @Test
    void shouldAwardPointsForCorrectAnswer() {
        // Given
        UserAnswer correctAnswer = new UserAnswer("player-1", "q1", 1, 5000);
        
        when(gameSessionRepository.findById("game-1")).thenReturn(Optional.of(session));
        when(quizRepository.findById("quiz-1")).thenReturn(Optional.of(quiz));

        // When
        int score = submitAnswerUseCase.execute("game-1", correctAnswer);

        // Then
        assertEquals(1000, score);
        assertEquals(1000, player.getScore());
        verify(gameSessionRepository).save(session);
        verify(playerResponseRepository).save(any(PlayerResponse.class));
        verify(achievementService).checkAndUnlockAchievements(eq(player), eq(correctAnswer), eq(true));
    }

    @Test
    void shouldAwardZeroPointsForIncorrectAnswer() {
        // Given
        UserAnswer incorrectAnswer = new UserAnswer("player-1", "q1", 0, 3000);

        when(gameSessionRepository.findById("game-1")).thenReturn(Optional.of(session));
        when(quizRepository.findById("quiz-1")).thenReturn(Optional.of(quiz));

        // When
        int score = submitAnswerUseCase.execute("game-1", incorrectAnswer);

        // Then
        assertEquals(0, score);
        assertEquals(0, player.getScore());
        verify(gameSessionRepository).save(session);
        verify(playerResponseRepository).save(any(PlayerResponse.class));
        verify(achievementService).checkAndUnlockAchievements(eq(player), eq(incorrectAnswer), eq(false));
    }
}
