package com.kubou.interface_adapter.controller;

import com.kubou.application.usecase.CreateGameUseCase;
import com.kubou.domain.entity.GameSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/games")
@SecurityRequirement(name = "bearerAuth")
public class GameCreationController {

    private final CreateGameUseCase createGameUseCase;

    public GameCreationController(CreateGameUseCase createGameUseCase) {
        this.createGameUseCase = createGameUseCase;
    }

    @PostMapping
    @Operation(summary = "Create a new game session")
    public GameSession createGame(@RequestBody CreateGameRequest request, Principal principal) {
        String hostId = principal.getName();
        return createGameUseCase.execute(request.getQuizId(), hostId);
    }

    public static class CreateGameRequest {
        private String quizId;

        public String getQuizId() {
            return quizId;
        }

        public void setQuizId(String quizId) {
            this.quizId = quizId;
        }
    }
}
