package com.kubou.interface_adapter.controller;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.application.service.TeamService;
import com.kubou.application.usecase.CreateGameUseCase;
import com.kubou.domain.entity.GameSession;
import com.kubou.domain.entity.GameState;
import com.kubou.domain.entity.ScoringSettings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/games")
@SecurityRequirement(name = "bearerAuth")
public class GameCreationController {

    private final CreateGameUseCase createGameUseCase;
    private final GameSessionRepository gameSessionRepository;
    private final TeamService teamService;

    public GameCreationController(CreateGameUseCase createGameUseCase, GameSessionRepository gameSessionRepository, TeamService teamService) {
        this.createGameUseCase = createGameUseCase;
        this.gameSessionRepository = gameSessionRepository;
        this.teamService = teamService;
    }

    @PostMapping
    @Operation(summary = "Create a new game session")
    public GameSession createGame(@RequestBody CreateGameRequest request, Principal principal) {
        String hostId = principal.getName();
        GameSession session = createGameUseCase.execute(request.getQuizId(), hostId);
        
        // Apply custom scoring settings if provided
        if (request.getScoringSettings() != null) {
            session.getGameConfig().setScoringSettings(request.getScoringSettings());
        }
        
        // Apply Team Mode
        if (request.isTeamMode()) {
            session.setTeamMode(true);
            // Initialize with default 2 teams
            teamService.initializeTeams(session, 2);
        }
        
        gameSessionRepository.save(session);
        return session;
    }

    @GetMapping("/by-pin/{pin}")
    @Operation(summary = "Get game session by PIN")
    public ResponseEntity<GameSession> getGameByPin(@PathVariable String pin) {
        return gameSessionRepository.findByPin(pin)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get game session by ID")
    public ResponseEntity<GameSession> getGameById(@PathVariable String id) {
        return gameSessionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-sessions")
    @Operation(summary = "Get all game sessions hosted by the current user")
    public ResponseEntity<List<GameSession>> getMySessions(Principal principal) {
        List<GameSession> sessions = gameSessionRepository.findByHostId(principal.getName());
        return ResponseEntity.ok(sessions);
    }
    
    @GetMapping("/participating")
    @Operation(summary = "Get all active game sessions where the current user is a player")
    public ResponseEntity<List<GameSession>> getParticipatingSessions(Principal principal) {
        // This works for both registered users (username) and guests (guest-UUID)
        // because we store the principal name in the userId field of PlayerData.
        List<GameSession> sessions = gameSessionRepository.findActiveSessionsByPlayerUserId(principal.getName());
        return ResponseEntity.ok(sessions);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Close/Delete a game session")
    public ResponseEntity<Void> closeSession(@PathVariable String id, Principal principal) {
        return gameSessionRepository.findById(id)
                .map(session -> {
                    if (!session.getHostId().equals(principal.getName())) {
                        return ResponseEntity.status(403).<Void>build();
                    }
                    // Instead of deleting, we mark it as FINISHED to keep history
                    session.setState(GameState.FINISHED);
                    gameSessionRepository.save(session);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public static class CreateGameRequest {
        private String quizId;
        private ScoringSettings scoringSettings;
        private boolean teamMode;

        public String getQuizId() {
            return quizId;
        }

        public void setQuizId(String quizId) {
            this.quizId = quizId;
        }

        public ScoringSettings getScoringSettings() {
            return scoringSettings;
        }

        public void setScoringSettings(ScoringSettings scoringSettings) {
            this.scoringSettings = scoringSettings;
        }

        public boolean isTeamMode() {
            return teamMode;
        }

        public void setTeamMode(boolean teamMode) {
            this.teamMode = teamMode;
        }
    }
}
