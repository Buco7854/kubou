package com.kubou.interface_adapter.controller;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.application.usecase.JoinGameUseCase;
import com.kubou.application.usecase.NextQuestionUseCase;
import com.kubou.application.usecase.StartGameUseCase;
import com.kubou.application.usecase.SubmitAnswerUseCase;
import com.kubou.domain.entity.*;
import com.kubou.interface_adapter.controller.dto.JoinLobbyRequest;
import com.kubou.interface_adapter.controller.dto.SubmitAnswerRequest;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class GameController {

    private final JoinGameUseCase joinGameUseCase;
    private final StartGameUseCase startGameUseCase;
    private final NextQuestionUseCase nextQuestionUseCase;
    private final SubmitAnswerUseCase submitAnswerUseCase;
    private final GameSessionRepository gameSessionRepository;
    private final QuizRepository quizRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public GameController(
            JoinGameUseCase joinGameUseCase,
            StartGameUseCase startGameUseCase,
            NextQuestionUseCase nextQuestionUseCase,
            SubmitAnswerUseCase submitAnswerUseCase,
            GameSessionRepository gameSessionRepository,
            QuizRepository quizRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.joinGameUseCase = joinGameUseCase;
        this.startGameUseCase = startGameUseCase;
        this.nextQuestionUseCase = nextQuestionUseCase;
        this.submitAnswerUseCase = submitAnswerUseCase;
        this.gameSessionRepository = gameSessionRepository;
        this.quizRepository = quizRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/lobby/join")
    public void joinLobby(@Payload JoinLobbyRequest request, SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            // Should not happen if frontend enforces guest login, but handle gracefully
            return;
        }

        String nickname = (String) headerAccessor.getSessionAttributes().get("nickname");
        
        // Determine if user is anonymous or authenticated
        String userId = null;
        if (!principal.getName().startsWith("guest-")) {
             userId = principal.getName();
        }

        String playerId = principal.getName();

        Player player = new Player(playerId, userId, nickname);
        joinGameUseCase.execute(request.getPin(), player);

        gameSessionRepository.findByPin(request.getPin()).ifPresent(session -> {
            // Broadcast to PIN topic (standard for players)
            String pinDestination = "/topic/lobby/" + request.getPin() + "/players";
            messagingTemplate.convertAndSend(pinDestination, session.getPlayers());
            
            // Broadcast to ID topic (for host monitoring)
            String idDestination = "/topic/lobby/" + session.getId() + "/players";
            messagingTemplate.convertAndSend(idDestination, session.getPlayers());

            // Send Game ID to the joining player so they can subscribe to game topics
            messagingTemplate.convertAndSendToUser(
                principal.getName(), 
                "/queue/lobby/joined", 
                Map.of("gameId", session.getId(), "pin", session.getPin())
            );
        });
    }

    @MessageMapping("/game/{gameId}/start")
    public void startGame(@DestinationVariable String gameId, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("L'utilisateur doit être authentifié pour démarrer une partie.");
        }
        GameSession session = startGameUseCase.execute(gameId, principal.getName());
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/started", "Game Started!");
        sendQuestion(session);
    }

    @MessageMapping("/game/{gameId}/next")
    public void nextQuestion(@DestinationVariable String gameId, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("L'utilisateur doit être authentifié pour passer à la question suivante.");
        }
        GameSession session = nextQuestionUseCase.execute(gameId, principal.getName());
        if (session.getState() == GameState.FINISHED) {
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/finished", "Game Over!");
        } else {
            sendQuestion(session);
        }
    }

    @MessageMapping("/game/{gameId}/submit")
    public void submitAnswer(@DestinationVariable String gameId, @Payload SubmitAnswerRequest request, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("L'utilisateur doit être authentifié pour soumettre une réponse.");
        }
        UserAnswer userAnswer = new UserAnswer(principal.getName(), request.getQuestionId(), request.getAnswerIndex(), request.getTimeToAnswerMs());
        int score = submitAnswerUseCase.execute(gameId, userAnswer);

        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/result", Map.of("pointsAwarded", score));
        broadcastLeaderboard(gameId);
    }

    private void sendQuestion(GameSession session) {
        Quiz quiz = quizRepository.findById(session.getQuizId()).orElseThrow();
        Question question = quiz.getQuestions().get(session.getCurrentQuestionIndex());
        
        Question clientQuestion = new Question(question.getId(), question.getText(), question.getOptions(), -1, question.getTags(), question.getDifficultyLevel());
        
        messagingTemplate.convertAndSend("/topic/game/" + session.getId() + "/question", clientQuestion);
    }

    private void broadcastLeaderboard(String gameId) {
        gameSessionRepository.findById(gameId).ifPresent(session -> {
            List<Player> leaderboard = session.getPlayers().stream()
                    .sorted(Comparator.comparingInt(Player::getScore).reversed())
                    .limit(5)
                    .collect(Collectors.toList());
            
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/leaderboard", leaderboard);
            
            if (session.isTeamMode()) {
                messagingTemplate.convertAndSend("/topic/game/" + gameId + "/teams", session.getTeams());
            }
        });
    }
}
