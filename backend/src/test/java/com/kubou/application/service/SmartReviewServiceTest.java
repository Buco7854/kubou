package com.kubou.application.service;

import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.application.repository.QuestionRepository;
import com.kubou.domain.entity.PlayerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class SmartReviewServiceTest {

    @Mock
    private PlayerResponseRepository playerResponseRepository;

    @Mock
    private QuestionRepository questionRepository;

    private SmartReviewService smartReviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        smartReviewService = new SmartReviewService(playerResponseRepository, questionRepository);
    }

    @Test
    void getIncorrectlyAnsweredQuestionIds_ShouldReturnOnlyIncorrectQuestionIds() {
        // Given
        String playerId = "player1";
        PlayerResponse correctResponse = new PlayerResponse("1", "session1", playerId, "q1", 1, true, 1000, 10, LocalDateTime.now());
        PlayerResponse incorrectResponse1 = new PlayerResponse("2", "session1", playerId, "q2", 2, false, 1500, 0, LocalDateTime.now());
        PlayerResponse incorrectResponse2 = new PlayerResponse("3", "session1", playerId, "q3", 0, false, 1200, 0, LocalDateTime.now());
        PlayerResponse correctResponseAgain = new PlayerResponse("4", "session2", playerId, "q4", 3, true, 800, 10, LocalDateTime.now());
        PlayerResponse incorrectResponseDuplicate = new PlayerResponse("5", "session2", playerId, "q2", 1, false, 2000, 0, LocalDateTime.now());


        List<PlayerResponse> responses = Arrays.asList(correctResponse, incorrectResponse1, incorrectResponse2, correctResponseAgain, incorrectResponseDuplicate);
        when(playerResponseRepository.findByPlayerId(playerId)).thenReturn(responses);

        // When
        List<String> incorrectQuestionIds = smartReviewService.getIncorrectlyAnsweredQuestionIds(playerId);

        // Then
        assertEquals(2, incorrectQuestionIds.size());
        assertTrue(incorrectQuestionIds.contains("q2"));
        assertTrue(incorrectQuestionIds.contains("q3"));
    }
}
