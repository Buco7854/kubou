package com.kubou.interface_adapter.controller;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.application.service.AchievementService;
import com.kubou.application.usecase.JoinGameUseCase;
import com.kubou.application.usecase.NextQuestionUseCase;
import com.kubou.application.usecase.StartGameUseCase;
import com.kubou.application.usecase.SubmitAnswerUseCase;
import com.kubou.application.usecase.dto.SubmitAnswerResult;
import com.kubou.domain.entity.*;
import com.kubou.interface_adapter.controller.dto.JoinLobbyRequest;
import com.kubou.interface_adapter.controller.dto.LeaderboardEntry;
import com.kubou.interface_adapter.controller.dto.SubmitAnswerRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    private final JoinGameUseCase joinGameUseCase;
    private final StartGameUseCase startGameUseCase;
    private final NextQuestionUseCase nextQuestionUseCase;
    private final SubmitAnswerUseCase submitAnswerUseCase;
    private final GameSessionRepository gameSessionRepository;
    private final QuizRepository quizRepository;
    private final PlayerResponseRepository playerResponseRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AchievementService achievementService;

    public GameController(
            JoinGameUseCase joinGameUseCase,
            StartGameUseCase startGameUseCase,
            NextQuestionUseCase nextQuestionUseCase,
            SubmitAnswerUseCase submitAnswerUseCase,
            GameSessionRepository gameSessionRepository,
            QuizRepository quizRepository,
            PlayerResponseRepository playerResponseRepository,
            SimpMessagingTemplate messagingTemplate,
            AchievementService achievementService
    ) {
        this.joinGameUseCase = joinGameUseCase;
        this.startGameUseCase = startGameUseCase;
        this.nextQuestionUseCase = nextQuestionUseCase;
        this.submitAnswerUseCase = submitAnswerUseCase;
        this.gameSessionRepository = gameSessionRepository;
        this.quizRepository = quizRepository;
        this.playerResponseRepository = playerResponseRepository;
        this.messagingTemplate = messagingTemplate;
        this.achievementService = achievementService;
    }

    // --- REST ENDPOINTS ---
    @PostMapping("/api/v1/games/{gameId}/finish-trigger")
    @ResponseBody
    public ResponseEntity<Void> triggerFinish(@PathVariable String gameId, Principal principal) {
        return gameSessionRepository.findById(gameId)
                .map(session -> {
                    achievementService.awardEndGameAchievements(session);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/api/v1/games/{gameId}/sync-time")
    @ResponseBody
    @Operation(summary = "Get current question start time for sync")
    public ResponseEntity<Map<String, Object>> getSyncTime(@PathVariable String gameId) {
        return gameSessionRepository.findById(gameId)
                .map(session -> {
                    Map<String, Object> response = new HashMap<>();
                    if (session.getCurrentQuestionStartTime() != null) {
                        response.put("startTime", session.getCurrentQuestionStartTime().toInstant(ZoneOffset.UTC).toEpochMilli());
                        response.put("serverTime", System.currentTimeMillis());
                    }
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/v1/games/{gameId}/progress")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProgress(@PathVariable String gameId) {
        return gameSessionRepository.findById(gameId)
                .map(session -> {
                    return quizRepository.findById(session.getQuizId())
                            .map(quiz -> {
                                if (session.getCurrentQuestionIndex() >= 0 && session.getCurrentQuestionIndex() < quiz.getQuestions().size()) {
                                    String questionId = quiz.getQuestions().get(session.getCurrentQuestionIndex()).getId();
                                    List<PlayerResponse> responses = playerResponseRepository.findByGameSessionIdAndQuestionId(gameId, questionId);
                                    long answeredCount = responses.stream().map(PlayerResponse::getPlayerId).distinct().count();
                                    int totalPlayers = session.getPlayers().size();
                                    
                                    Map<String, Object> response = new HashMap<>();
                                    response.put("answeredCount", answeredCount);
                                    response.put("totalPlayers", totalPlayers);
                                    return ResponseEntity.ok(response);
                                }
                                Map<String, Object> emptyResponse = new HashMap<>();
                                emptyResponse.put("answeredCount", 0L);
                                emptyResponse.put("totalPlayers", session.getPlayers().size());
                                return ResponseEntity.ok(emptyResponse);
                            })
                            .orElse(ResponseEntity.notFound().build());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // --- WEBSOCKET HANDLERS ---

    @MessageMapping("/lobby/join")
    public void joinLobby(@Payload JoinLobbyRequest request, SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) return;

        String nickname = (String) headerAccessor.getSessionAttributes().get("nickname");
        String userId = principal.getName();
        String playerId = UUID.randomUUID().toString();

        Player player = new Player(playerId, userId, nickname);
        joinGameUseCase.execute(request.getPin(), player);

        gameSessionRepository.findByPin(request.getPin()).ifPresent(session -> {
            String pinDestination = "/topic/lobby/" + request.getPin() + "/players";
            messagingTemplate.convertAndSend(pinDestination, session.getPlayers());

            String idDestination = "/topic/lobby/" + session.getId() + "/players";
            messagingTemplate.convertAndSend(idDestination, session.getPlayers());

            // Broadcast progress update to host if game is in progress
            if (session.getState() == GameState.IN_PROGRESS) {
                try {
                    Quiz quiz = quizRepository.findById(session.getQuizId()).orElseThrow();
                    long answeredCount = 0;
                    if (session.getCurrentQuestionIndex() >= 0 && session.getCurrentQuestionIndex() < quiz.getQuestions().size()) {
                        String questionId = quiz.getQuestions().get(session.getCurrentQuestionIndex()).getId();
                        List<PlayerResponse> responses = playerResponseRepository.findByGameSessionIdAndQuestionId(session.getId(), questionId);
                        answeredCount = responses.stream().map(PlayerResponse::getPlayerId).distinct().count();
                    }

                    messagingTemplate.convertAndSend("/topic/game/" + session.getId() + "/progress", Map.of(
                        "answeredCount", answeredCount,
                        "totalPlayers", session.getPlayers().size()
                    ));

                } catch (Exception e) {
                    logger.error("Error updating progress on join", e);
                }
            }

            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/lobby/joined",
                Map.of(
                    "gameId", session.getId(),
                    "pin", session.getPin(),
                    "state", session.getState()
                )
            );
        });
    }

    @MessageMapping("/game/{gameId}/start")
    public void startGame(@DestinationVariable String gameId, Principal principal) {
        if (principal == null) throw new IllegalStateException("Auth required");
        GameSession session = startGameUseCase.execute(gameId, principal.getName());
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/started", "Game Started!");
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
        
        // Execute state transition
        GameSession session = nextQuestionUseCase.execute(gameId, principal.getName());
        
        // Handle the NEW state
        if (session.getState() == GameState.FINISHED) {
            broadcastLeaderboard(gameId);
            try {
                achievementService.awardEndGameAchievements(session);
            } catch (Exception e) {
                logger.error("Failed to award achievements for gameId: {}", gameId, e);
            }
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/finished", "Game Over!");
            
        } else if (session.getState() == GameState.QUESTION_RESULTS) {
             // We just transitioned TO results (meaning the round ended)
             // We must broadcast the correct answer and leaderboard NOW.
             broadcastCorrectAnswer(gameId, session);
             broadcastLeaderboard(gameId);
             messagingTemplate.convertAndSend("/topic/game/" + gameId + "/round_end", "Round Finished");
             
        } else if (session.getState() == GameState.IN_PROGRESS) {
            // We just transitioned TO in_progress (meaning a NEW question started)
            sendQuestion(session);
        }
    }

    @MessageMapping("/game/{gameId}/submit")
    public void submitAnswer(@DestinationVariable String gameId, @Payload SubmitAnswerRequest request, Principal principal) {
        if (principal == null) throw new IllegalStateException("Auth required");
        
        try {
            GameSession session = gameSessionRepository.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("Game session not found"));
                    
            if (session.getHostId().equals(principal.getName())) return;
            
            String userId = principal.getName();
            Player player = session.getPlayers().stream()
                    .filter(p -> userId.equals(p.getUserId()) || (p.getUserId() == null && userId.equals(p.getId())))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Player not found in session"));


            // --- BACKEND TIMER VALIDATION ---
            // Validate against server start time if available
            if (session.getCurrentQuestionStartTime() != null) {
                long elapsed = System.currentTimeMillis() - session.getCurrentQuestionStartTime().toInstant(ZoneOffset.UTC).toEpochMilli();
                // Allow 30s + 5s grace period
                if (elapsed > 35000) {
                    messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/result", Map.of("pointsAwarded", 0, "error", "Time expired"));
                    return;
                }
            } else {
                // Fallback if start time not set (should not happen with new logic)
                if (request.getTimeToAnswerMs() > 35000 || request.getTimeToAnswerMs() < 0) {
                    messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/result", Map.of("pointsAwarded", 0, "error", "Time invalid"));
                    return;
                }
            }

            UserAnswer userAnswer = new UserAnswer(player.getId(), request.getQuestionId(), request.getAnswerIndex(), request.getTimeToAnswerMs());
            SubmitAnswerResult result = submitAnswerUseCase.execute(gameId, userAnswer);

            // 2. ENVOI DES INFOS WS (CRITIQUE POUR L'UI)
            // A. Info pour l'hôte (un joueur a répondu)
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/host/answer_received", Map.of("playerId", player.getId()));

            // B. Info pour le joueur (ses points)
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/result", Map.of("pointsAwarded", result.getScore()));
            
            // C. Info globale (progression)
            broadcastProgress(gameId, session, result.isRoundFinished());
            
            // 3. GESTION DES BADGES (NON-CRITIQUE)
            try {
                achievementService.checkAndUnlockAchievements(player, userAnswer, result.isCorrect());
            } catch (Exception e) {
                logger.error("ACHIEVEMENT ERROR for player {}: {}", player.getId(), e.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error in submitAnswer for gameId: {}", gameId, e);
        }
    }

    private void broadcastProgress(String gameId, GameSession session, boolean isRoundFinished) {
        try {
            int totalPlayers = session.getPlayers().size();
            Quiz quiz = quizRepository.findById(session.getQuizId()).orElseThrow();
            String currentQuestionId = quiz.getQuestions().get(session.getCurrentQuestionIndex()).getId();
            List<PlayerResponse> responses = playerResponseRepository.findByGameSessionIdAndQuestionId(gameId, currentQuestionId);
            long answeredCount = responses.stream().map(PlayerResponse::getPlayerId).distinct().count();

            logger.info("Broadcasting progress for game {}: {}/{} (Round Finished: {})", gameId, answeredCount, totalPlayers, isRoundFinished);
            
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/progress", Map.of(
                "answeredCount", answeredCount,
                "totalPlayers", totalPlayers
            ));
            
            if (isRoundFinished) {
                GameSession freshSession = gameSessionRepository.findById(gameId).orElseThrow();
                broadcastCorrectAnswer(gameId, freshSession);
                broadcastLeaderboard(gameId);
                messagingTemplate.convertAndSend("/topic/game/" + gameId + "/round_end", "Round Finished");
            }
        } catch (Exception e) {
            logger.error("Error broadcasting progress for gameId: {}", gameId, e);
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
                
                // Broadcast progress update to host if game is in progress
                if (session.getState() == GameState.IN_PROGRESS) {
                    // Better: Fetch current question ID to get answered count
                    try {
                        Quiz quiz = quizRepository.findById(session.getQuizId()).orElseThrow();
                        if (session.getCurrentQuestionIndex() >= 0 && session.getCurrentQuestionIndex() < quiz.getQuestions().size()) {
                            String questionId = quiz.getQuestions().get(session.getCurrentQuestionIndex()).getId();
                            List<PlayerResponse> responses = playerResponseRepository.findByGameSessionIdAndQuestionId(gameId, questionId);
                            long answeredCount = responses.stream().map(PlayerResponse::getPlayerId).distinct().count();
                            
                            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/progress", Map.of(
                                "answeredCount", answeredCount,
                                "totalPlayers", session.getPlayers().size()
                            ));
                        }
                    } catch (Exception e) {
                        logger.error("Error updating progress on leave", e);
                    }
                }
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
                broadcastLeaderboard(gameId);
                try {
                    achievementService.awardEndGameAchievements(session);
                } catch (Exception e) {
                    logger.error("Failed to award achievements for gameId: {}", gameId, e);
                }
                messagingTemplate.convertAndSend("/topic/game/" + gameId + "/finished", "Session closed by host.");
            }
        });
    }

    private void sendQuestion(GameSession session) {
        Quiz quiz = quizRepository.findById(session.getQuizId()).orElseThrow();
        Question question = quiz.getQuestions().get(session.getCurrentQuestionIndex());
        
        // Set start time
        session.setCurrentQuestionStartTime(LocalDateTime.now(ZoneOffset.UTC));
        gameSessionRepository.save(session);
        
        long startTimeEpoch = session.getCurrentQuestionStartTime().toInstant(ZoneOffset.UTC).toEpochMilli();
        
        // Send question to players (without correct answer)
        Question clientQuestion = new Question(question.getId(), question.getText(), question.getOptions(), -1, question.getTags(), question.getDifficultyLevel());
        
        // Wrap question with metadata including start time
        Map<String, Object> clientPayload = new HashMap<>();
        clientPayload.put("id", clientQuestion.getId());
        clientPayload.put("text", clientQuestion.getText());
        clientPayload.put("options", clientQuestion.getOptions());
        clientPayload.put("difficultyLevel", clientQuestion.getDifficultyLevel());
        clientPayload.put("startTime", startTimeEpoch);
        
        messagingTemplate.convertAndSend("/topic/game/" + session.getId() + "/question", clientPayload);
        
        Map<String, Object> hostQuestionPayload = new HashMap<>();
        hostQuestionPayload.put("id", question.getId());
        hostQuestionPayload.put("text", question.getText());
        hostQuestionPayload.put("options", question.getOptions());
        hostQuestionPayload.put("correctAnswerIndex", -1);
        hostQuestionPayload.put("tags", question.getTags());
        hostQuestionPayload.put("difficultyLevel", question.getDifficultyLevel());
        hostQuestionPayload.put("isHost", true);
        hostQuestionPayload.put("startTime", startTimeEpoch);

        messagingTemplate.convertAndSend("/topic/game/" + session.getId() + "/host/question", hostQuestionPayload);
    }

    private void broadcastLeaderboard(String gameId) {
        gameSessionRepository.findById(gameId).ifPresent(session -> {
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