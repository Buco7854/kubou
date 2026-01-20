package com.kubou.interface_adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubou.application.repository.GameSessionRepository;
import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.application.service.AchievementService;
import com.kubou.application.usecase.JoinGameUseCase;
import com.kubou.application.usecase.NextQuestionUseCase;
import com.kubou.application.usecase.StartGameUseCase;
import com.kubou.application.usecase.SubmitAnswerUseCase;
import com.kubou.domain.entity.GameSession;
import com.kubou.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
@AutoConfigureMockMvc(addFilters = false)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JoinGameUseCase joinGameUseCase;
    @MockBean
    private StartGameUseCase startGameUseCase;
    @MockBean
    private NextQuestionUseCase nextQuestionUseCase;
    @MockBean
    private SubmitAnswerUseCase submitAnswerUseCase;
    @MockBean
    private GameSessionRepository gameSessionRepository;
    @MockBean
    private QuizRepository quizRepository;
    @MockBean
    private PlayerResponseRepository playerResponseRepository;
    @MockBean
    private SimpMessagingTemplate messagingTemplate;
    @MockBean
    private AchievementService achievementService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider; // Mocked to satisfy dependency injection

    @Test
    @WithMockUser(username = "host")
    void triggerFinish_ShouldCallService_WhenSessionExists() throws Exception {
        GameSession session = new GameSession("g1", "123456", "q1", "host");
        when(gameSessionRepository.findById("g1")).thenReturn(Optional.of(session));

        mockMvc.perform(post("/api/v1/games/g1/finish-trigger"))
                .andExpect(status().isOk());

        verify(achievementService).awardEndGameAchievements(session);
    }

    @Test
    @WithMockUser(username = "host")
    void triggerFinish_ShouldReturnNotFound_WhenSessionMissing() throws Exception {
        when(gameSessionRepository.findById("g1")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/games/g1/finish-trigger"))
                .andExpect(status().isNotFound());
    }
}
