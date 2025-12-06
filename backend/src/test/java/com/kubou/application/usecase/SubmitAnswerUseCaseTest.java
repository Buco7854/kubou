package com.kubou.application.usecase;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.domain.entity.*;
import com.kubou.domain.service.IScoringStrategy;
import com.kubou.domain.service.SimpleScoringStrategy;
import com.kubou.infrastructure.repository.InMemoryGameSessionRepository;
import com.kubou.infrastructure.repository.InMemoryQuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SubmitAnswerUseCaseTest {

    private SubmitAnswerUseCase submitAnswerUseCase;
    private GameSessionRepository gameSessionRepository;
    private QuizRepository quizRepository;
    private IScoringStrategy scoringStrategy;

    private GameSession session;
    private Player player;
    private Quiz quiz;
    private Question question;

    @BeforeEach
    void setUp() {
        gameSessionRepository = new InMemoryGameSessionRepository();
        quizRepository = new InMemoryQuizRepository();
        scoringStrategy = new SimpleScoringStrategy(); // Using the simple strategy for this test

        submitAnswerUseCase = new SubmitAnswerUseCase(gameSessionRepository, quizRepository, scoringStrategy);

        // Setup test data
        player = new Player("player-1", "TestPlayer");
        question = new Question("q1", "2+2?", Arrays.asList("3", "4", "5"), 1, Collections.emptyList(), 1);
        quiz = new Quiz("quiz-1", "Math Quiz", Collections.singletonList(question));
        session = new GameSession("game-1", "123456", "quiz-1", "host-1");
        session.addPlayer(player);

        quizRepository.save(quiz);
        gameSessionRepository.save(session);
    }

    @Test
    void shouldAwardPointsForCorrectAnswer() {
        // Given
        UserAnswer correctAnswer = new UserAnswer("player-1", "q1", 1, 5000);

        // When
        int score = submitAnswerUseCase.execute("game-1", correctAnswer);

        // Then
        assertEquals(1000, score);
        GameSession updatedSession = gameSessionRepository.findById("game-1").orElseThrow();
        Player updatedPlayer = updatedSession.getPlayers().get(0);
        assertEquals(1000, updatedPlayer.getScore());
    }

    @Test
    void shouldAwardZeroPointsForIncorrectAnswer() {
        // Given
        UserAnswer incorrectAnswer = new UserAnswer("player-1", "q1", 0, 3000);

        // When
        int score = submitAnswerUseCase.execute("game-1", incorrectAnswer);

        // Then
        assertEquals(0, score);
        GameSession updatedSession = gameSessionRepository.findById("game-1").orElseThrow();
        Player updatedPlayer = updatedSession.getPlayers().get(0);
        assertEquals(0, updatedPlayer.getScore());
    }
}
