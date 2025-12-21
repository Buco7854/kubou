package com.kubou.interface_adapter.controller;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.application.usecase.JoinGameUseCase;
import com.kubou.application.usecase.NextQuestionUseCase;
import com.kubou.application.usecase.StartGameUseCase;
import com.kubou.application.usecase.SubmitAnswerUseCase;
import com.kubou.domain.entity.*;
import com.kubou.interface_adapter.controller.dto.JoinLobbyRequest;
import com.kubou.interface_adapter.controller.dto.LeaderboardEntry;
import com.kubou.interface_adapter.controller.dto.SubmitAnswerRequest;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Comparator;
import java.util.HashMap;
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
    private final PlayerResponseRepository playerResponseRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public GameController(
            JoinGameUseCase joinGameUseCase,
            StartGameUseCase startGameUseCase,
            NextQuestionUseCase nextQuestionUseCase,
            SubmitAnswerUseCase submitAnswerUseCase,
            GameSessionRepository gameSessionRepository,
            QuizRepository quizRepository,
            PlayerResponseRepository playerResponseRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.joinGameUseCase = joinGameUseCase;
        this.startGameUseCase = startGameUseCase;
        this.nextQuestionUseCase = nextQuestionUseCase;
        this.submitAnswerUseCase = submitAnswerUseCase;
        this.gameSessionRepository = gameSessionRepository;
        this.quizRepository = quizRepository;
        this.playerResponseRepository = playerResponseRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/lobby/join")
    public void joinLobby(@Payload JoinLobbyRequest request, SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            return;
        }

        String nickname = (String) headerAccessor.getSessionAttributes().get("nickname");
        
        String userId = null;
        if (!principal.getName().startsWith("guest-")) {
             userId = principal.getName();
        } else {
            userId = principal.getName();
        }

        String playerId = UUID.randomUUID().toString();

        Player player = new Player(playerId, userId, nickname);
        joinGameUseCase.execute(request.getPin(), player);

        gameSessionRepository.findByPin(request.getPin()).ifPresent(session -> {
            String pinDestination = "/topic/lobby/" + request.getPin() + "/players";
            messagingTemplate.convertAndSend(pinDestination, session.getPlayers());
            
            String idDestination = "/topic/lobby/" + session.getId() + "/players";
            messagingTemplate.convertAndSend(idDestination, session.getPlayers());

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
        
        // Broadcast initial teams info if team mode
        if (session.isTeamMode()) {
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/teams", session.getTeams());
        }
        
        sendQuestion(session);
    }

    @MessageMapping("/game/{gameId}/next")
    public void nextQuestion(@DestinationVariable String gameId, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("L'utilisateur doit être authentifié pour passer à la question suivante.");
        }
        GameSession session = nextQuestionUseCase.execute(gameId, principal.getName());
        
        if (session.getState() == GameState.FINISHED) {
            // Send final leaderboard and podium
            broadcastLeaderboard(gameId);
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/finished", "Game Over!");
        } else if (session.getState() == GameState.QUESTION_RESULTS) {
             // Transitioned to Results (End of Question)
             broadcastCorrectAnswer(gameId, session);
             broadcastLeaderboard(gameId);
             messagingTemplate.convertAndSend("/topic/game/" + gameId + "/round_end", "Round Finished");
        } else {
            // Transitioned to In Progress (Next Question)
            sendQuestion(session);
        }
    }

    @MessageMapping("/game/{gameId}/submit")
    public void submitAnswer(@DestinationVariable String gameId, @Payload SubmitAnswerRequest request, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("L'utilisateur doit être authentifié pour soumettre une réponse.");
        }
        
        GameSession session = gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found"));
                
        if (session.getHostId().equals(principal.getName())) {
            return;
        }
        
        // Find player ID from principal
        String userId = principal.getName();
        Player player = session.getPlayers().stream()
                .filter(p -> userId.equals(p.getUserId()) || (p.getUserId() == null && userId.equals(p.getId())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Player not found in session"));

        UserAnswer userAnswer = new UserAnswer(player.getId(), request.getQuestionId(), request.getAnswerIndex(), request.getTimeToAnswerMs());
        int score = submitAnswerUseCase.execute(gameId, userAnswer);

        // Notify host that a player answered (without revealing answer)
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/host/answer_received", Map.of("playerId", player.getId()));

        // Send immediate ack to user
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/result", Map.of("pointsAwarded", score));
        
        // Check if all players have answered
        // We need to re-fetch session to get updated state if SubmitAnswerUseCase changed it
        GameSession updatedSession = gameSessionRepository.findById(gameId).orElseThrow();
        
        List<PlayerResponse> responses = playerResponseRepository.findByGameSessionIdAndQuestionId(gameId, request.getQuestionId());
        int totalPlayers = updatedSession.getPlayers().size();
        // Count unique players who answered
        long answeredCount = responses.stream().map(PlayerResponse::getPlayerId).distinct().count();
        
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/progress", Map.of(
            "answeredCount", answeredCount,
            "totalPlayers", totalPlayers
        ));
        
        if (updatedSession.getState() == GameState.QUESTION_RESULTS) {
            // All players answered, trigger end of round
            broadcastCorrectAnswer(gameId, updatedSession);
            broadcastLeaderboard(gameId);
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/round_end", "Round Finished");
        }
    }

    @MessageMapping("/game/{gameId}/leave")
    public void leaveGame(@DestinationVariable String gameId, Principal principal) {
        if (principal == null) return;

        gameSessionRepository.findById(gameId).ifPresent(session -> {
            String userId = principal.getName();
            List<Player> playersToRemove = session.getPlayers().stream()
                    .filter(p -> userId.equals(p.getUserId()) || (p.getUserId() == null && userId.equals(p.getId()))) 
                    .collect(Collectors.toList());
            
            if (!playersToRemove.isEmpty()) {
                session.getPlayers().removeAll(playersToRemove);
                gameSessionRepository.save(session);
                
                String pinDestination = "/topic/lobby/" + session.getPin() + "/players";
                messagingTemplate.convertAndSend(pinDestination, session.getPlayers());
                
                String idDestination = "/topic/lobby/" + session.getId() + "/players";
                messagingTemplate.convertAndSend(idDestination, session.getPlayers());
            }
        });
    }

    @MessageMapping("/game/{gameId}/close")
    public void closeGame(@DestinationVariable String gameId, Principal principal) {
        if (principal == null) return;

        gameSessionRepository.findById(gameId).ifPresent(session -> {
            if (session.getHostId().equals(principal.getName())) {
                session.setState(GameState.FINISHED);
                gameSessionRepository.save(session);
                messagingTemplate.convertAndSend("/topic/game/" + gameId + "/finished", "Session closed by host.");
            }
        });
    }

    private void sendQuestion(GameSession session) {
        Quiz quiz = quizRepository.findById(session.getQuizId()).orElseThrow();
        Question question = quiz.getQuestions().get(session.getCurrentQuestionIndex());
        
        // Send question to players (without correct answer)
        Question clientQuestion = new Question(question.getId(), question.getText(), question.getOptions(), -1, question.getTags(), question.getDifficultyLevel());
        messagingTemplate.convertAndSend("/topic/game/" + session.getId() + "/question", clientQuestion);
        
        // Send question to host (WITHOUT correct answer as requested)
        // We use a Map to add extra metadata if needed by frontend
        Map<String, Object> hostQuestionPayload = new HashMap<>();
        hostQuestionPayload.put("id", question.getId());
        hostQuestionPayload.put("text", question.getText());
        hostQuestionPayload.put("options", question.getOptions());
        hostQuestionPayload.put("correctAnswerIndex", -1); // Hidden
        hostQuestionPayload.put("tags", question.getTags());
        hostQuestionPayload.put("difficultyLevel", question.getDifficultyLevel());
        hostQuestionPayload.put("isHost", true); // Hint for frontend to render differently if needed (e.g. disabled buttons but with colors)

        messagingTemplate.convertAndSend("/topic/game/" + session.getId() + "/host/question", hostQuestionPayload);
    }

    private void broadcastLeaderboard(String gameId) {
        gameSessionRepository.findById(gameId).ifPresent(session -> {
            // Sort players by score
            List<Player> sortedPlayers = session.getPlayers().stream()
                    .sorted(Comparator.comparingInt(Player::getScore).reversed())
                    .collect(Collectors.toList());
            
            List<LeaderboardEntry> leaderboard = sortedPlayers.stream().map(player -> {
                String teamName = null;
                if (session.isTeamMode()) {
                    teamName = session.getTeams().stream()
                            .filter(t -> t.getPlayerIds().contains(player.getId()))
                            .findFirst()
                            .map(Team::getName)
                            .orElse(null);
                }
                return new LeaderboardEntry(player.getUserId(), player.getNickname(), player.getScore(), player.getCurrentStreak(), teamName);
            }).collect(Collectors.toList());
            
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/leaderboard", leaderboard);
            
            if (session.isTeamMode()) {
                messagingTemplate.convertAndSend("/topic/game/" + gameId + "/teams", session.getTeams());
            }
        });
    }

    private void broadcastCorrectAnswer(String gameId, GameSession session) {
        Quiz quiz = quizRepository.findById(session.getQuizId()).orElseThrow();
        Question question = quiz.getQuestions().get(session.getCurrentQuestionIndex());
        
        String correctAnswerText = "";
        if (question.getOptions() != null && question.getCorrectAnswerIndex() >= 0 && question.getCorrectAnswerIndex() < question.getOptions().size()) {
            correctAnswerText = question.getOptions().get(question.getCorrectAnswerIndex());
        }

        Map<String, Object> payload = Map.of(
            "questionId", question.getId(),
            "correctAnswerIndex", question.getCorrectAnswerIndex(),
            "correctAnswerText", correctAnswerText
        );
        
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/answer", payload);
    }
}
