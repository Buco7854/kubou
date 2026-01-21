package com.kubou.application.usecase;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.application.usecase.dto.SubmitAnswerResult;
import com.kubou.domain.entity.*;
import com.kubou.domain.service.IScoringStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class SubmitAnswerUseCaseTest {

    @Mock private GameSessionRepository gameSessionRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private PlayerResponseRepository playerResponseRepository;
    @Mock private IScoringStrategy scoringStrategy;

    private SubmitAnswerUseCase submitAnswerUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        submitAnswerUseCase = new SubmitAnswerUseCase(
            gameSessionRepository, quizRepository, playerResponseRepository, scoringStrategy
        );
    }

    @Test
    void execute_ShouldSaveScore_Correctly() {
        // Arrange
        String gameId = "g1";
        String playerId = "p1";
        String questionId = "q1";
        
        GameSession session = new GameSession(gameId, "123", "quiz1", "host");
        Player player = new Player(playerId, "user1", "Nick");
        session.getPlayers().add(player);
        // Add a second player so round is not finished
        session.getPlayers().add(new Player("p2", "user2", "Player 2"));
        
        Quiz quiz = new Quiz("quiz1", "Title", new ArrayList<>());
        Question question = new Question(questionId, "Text", Collections.emptyList(), 0, Collections.emptyList(), 1);
        quiz.getQuestions().add(question);
        
        UserAnswer userAnswer = new UserAnswer(playerId, questionId, 0, 1000);
        
        when(gameSessionRepository.findById(gameId)).thenReturn(Optional.of(session));
        when(quizRepository.findById("quiz1")).thenReturn(Optional.of(quiz));
        when(scoringStrategy.calculate(any(), any(), any(), anyInt())).thenReturn(100);
        
        // Act
        SubmitAnswerResult result = submitAnswerUseCase.execute(gameId, userAnswer);

        // Assert
        assertEquals(100, result.getScore());
        assertEquals(100, player.getScore());
        assertFalse(result.isRoundFinished());
        
        verify(gameSessionRepository).save(session);
        verify(playerResponseRepository).save(any(PlayerResponse.class));
    }

    @Test
    void execute_ShouldHandleGameNotInProgress() {
        // Arrange
        String gameId = "g1";
        String playerId = "p1";
        String questionId = "q1";
        
        GameSession session = new GameSession(gameId, "123", "quiz1", "host");
        session.setState(GameState.FINISHED); // Not IN_PROGRESS
        Player player = new Player(playerId, "user1", "Nick");
        session.getPlayers().add(player);
        
        Quiz quiz = new Quiz("quiz1", "Title", new ArrayList<>());
        Question question = new Question(questionId, "Text", Collections.emptyList(), 0, Collections.emptyList(), 1);
        quiz.getQuestions().add(question);
        
        UserAnswer userAnswer = new UserAnswer(playerId, questionId, 0, 1000);
        
        when(gameSessionRepository.findById(gameId)).thenReturn(Optional.of(session));
        when(quizRepository.findById("quiz1")).thenReturn(Optional.of(quiz));
        when(scoringStrategy.calculate(any(), any(), any(), anyInt())).thenReturn(100);
        
        // Act
        SubmitAnswerResult result = submitAnswerUseCase.execute(gameId, userAnswer);

        // Assert
        assertEquals(100, result.getScore());
        verify(gameSessionRepository).save(session);
    }
}