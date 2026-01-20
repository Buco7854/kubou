package com.kubou.interface_adapter.controller;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.application.service.AchievementService;
import com.kubou.application.usecase.JoinGameUseCase;
import com.kubou.application.usecase.NextQuestionUseCase;
import com.kubou.application.usecase.StartGameUseCase;
import com.kubou.application.usecase.SubmitAnswerUseCase;
import com.kubou.domain.entity.GameSession;
import com.kubou.domain.entity.GameState;
import com.kubou.domain.entity.Player;
import com.kubou.domain.entity.Quiz;
import com.kubou.interface_adapter.controller.dto.JoinLobbyRequest;
import com.kubou.interface_adapter.controller.dto.SubmitAnswerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GameControllerTest {

    @Mock private JoinGameUseCase joinGameUseCase;
    @Mock private StartGameUseCase startGameUseCase;
    @Mock private NextQuestionUseCase nextQuestionUseCase;
    @Mock private SubmitAnswerUseCase submitAnswerUseCase;
    @Mock private GameSessionRepository gameSessionRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private PlayerResponseRepository playerResponseRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private AchievementService achievementService;
    
    private GameController gameController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gameController = new GameController(
            joinGameUseCase, startGameUseCase, nextQuestionUseCase, submitAnswerUseCase,
            gameSessionRepository, quizRepository, playerResponseRepository,
            messagingTemplate, achievementService
        );
    }

    @Test
    void triggerFinish_ShouldCallService_WhenSessionExists() {
        GameSession session = new GameSession("g1", "123456", "q1", "host");
        when(gameSessionRepository.findById("g1")).thenReturn(Optional.of(session));

        ResponseEntity<Void> response = gameController.triggerFinish("g1", mock(Principal.class));

        assertEquals(200, response.getStatusCodeValue());
        verify(achievementService).awardEndGameAchievements(session);
    }

    @Test
    void joinLobby_ShouldBroadcast_WhenUserIsAuthenticated() {
        JoinLobbyRequest request = new JoinLobbyRequest();
        request.setPin("123456");
        
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user1");
        when(headerAccessor.getUser()).thenReturn(principal);
        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of("nickname", "Nick"));

        GameSession session = new GameSession("g1", "123456", "q1", "host");
        when(gameSessionRepository.findByPin("123456")).thenReturn(Optional.of(session));

        gameController.joinLobby(request, headerAccessor);

        verify(joinGameUseCase).execute(eq("123456"), any(Player.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/lobby/123456/players"), any(Object.class));
    }

    @Test
    void startGame_ShouldBroadcast_WhenUserIsHost() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("host");
        
        GameSession session = new GameSession("g1", "123456", "q1", "host");
        session.setCurrentQuestionIndex(0); // Ensure index is 0 to avoid IndexOutOfBoundsException
        
        Quiz quiz = new Quiz("q1", "Title", new ArrayList<>());
        quiz.getQuestions().add(new com.kubou.domain.entity.Question("q1", "Text", Collections.emptyList(), 0, Collections.emptyList(), 1));
        
        when(startGameUseCase.execute("g1", "host")).thenReturn(session);
        when(quizRepository.findById("q1")).thenReturn(Optional.of(quiz));

        gameController.startGame("g1", principal);

        verify(messagingTemplate).convertAndSend(eq("/topic/game/g1/started"), anyString());
    }

    @Test
    void nextQuestion_ShouldAwardAchievements_WhenFinished() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("host");
        
        GameSession session = new GameSession("g1", "123456", "q1", "host");
        session.setState(GameState.FINISHED);
        
        when(nextQuestionUseCase.execute("g1", "host")).thenReturn(session);
        when(gameSessionRepository.findById("g1")).thenReturn(Optional.of(session)); // For broadcastLeaderboard

        gameController.nextQuestion("g1", principal);

        verify(achievementService).awardEndGameAchievements(session);
        verify(messagingTemplate).convertAndSend(eq("/topic/game/g1/finished"), anyString());
    }

    @Test
    void submitAnswer_ShouldBroadcastProgress_WhenUserIsPlayer() {
        // Arrange
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("player1");
        
        GameSession session = new GameSession("g1", "123456", "q1", "host");
        session.setCurrentQuestionIndex(0);
        Player player = new Player("p1", "player1", "Nick");
        session.getPlayers().add(player);
        
        Quiz quiz = new Quiz("q1", "Title", new ArrayList<>());
        quiz.getQuestions().add(new com.kubou.domain.entity.Question("q1", "Text", Collections.emptyList(), 0, Collections.emptyList(), 1));
        
        when(gameSessionRepository.findById("g1")).thenReturn(Optional.of(session));
        when(quizRepository.findById("q1")).thenReturn(Optional.of(quiz));
        when(submitAnswerUseCase.execute(anyString(), any())).thenReturn(100);
        when(playerResponseRepository.findByGameSessionIdAndQuestionId("g1", "q1")).thenReturn(Collections.emptyList());
        
        SubmitAnswerRequest request = new SubmitAnswerRequest();
        request.setQuestionId("q1");
        request.setAnswerIndex(0);
        request.setTimeToAnswerMs(1000);

        // Act
        gameController.submitAnswer("g1", request, principal);

        // Assert
        verify(submitAnswerUseCase).execute(eq("g1"), any());
        // Verify progress broadcast
        verify(messagingTemplate).convertAndSend(eq("/topic/game/g1/progress"), any(Map.class));
        // Verify host notification
        verify(messagingTemplate).convertAndSend(eq("/topic/game/g1/host/answer_received"), any(Map.class));
    }
}
