package com.kubou.application.usecase;

import com.kubou.domain.entity.Player;
import com.kubou.infrastructure.repository.jpa.GameSessionJpaRepository;
import com.kubou.infrastructure.repository.jpa.mapper.GameSessionMapper;
import com.kubou.infrastructure.repository.jpa.model.GameSessionData;
import com.kubou.infrastructure.repository.jpa.model.PlayerData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JoinGameUseCaseTest {

    private JoinGameUseCase joinGameUseCase;

    @Mock
    private GameSessionJpaRepository gameSessionJpaRepository;

    @Mock
    private GameSessionMapper gameSessionMapper;

    private GameSessionData existingSessionData;

    @BeforeEach
    void setUp() {
        joinGameUseCase = new JoinGameUseCase(gameSessionJpaRepository, gameSessionMapper);

        // Create a pre-existing game session data for players to join
        existingSessionData = new GameSessionData();
        existingSessionData.setId("game-1");
        existingSessionData.setPin("123456");
        existingSessionData.setQuizId("quiz-1");
        existingSessionData.setHostId("host-1");
        existingSessionData.setPlayers(new ArrayList<>());
    }

    @Test
    void shouldAddPlayerToExistingGameSession() {
        // Given
        Player newPlayer = new Player("player-1", "John");
        String gamePin = "123456";
        PlayerData newPlayerData = new PlayerData();
        newPlayerData.setId("player-1");
        newPlayerData.setNickname("John");

        when(gameSessionJpaRepository.findByPin(gamePin)).thenReturn(Optional.of(existingSessionData));
        when(gameSessionMapper.toData(newPlayer)).thenReturn(newPlayerData);

        // When
        joinGameUseCase.execute(gamePin, newPlayer);

        // Then
        assertEquals(1, existingSessionData.getPlayers().size());
        assertEquals("player-1", existingSessionData.getPlayers().get(0).getId());
        assertEquals("John", existingSessionData.getPlayers().get(0).getNickname());
    }

    @Test
    void shouldThrowExceptionWhenJoiningNonExistentGame() {
        // Given
        Player newPlayer = new Player("player-2", "Jane");
        String invalidPin = "999999";

        when(gameSessionJpaRepository.findByPin(invalidPin)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            joinGameUseCase.execute(invalidPin, newPlayer);
        }, "Game session not found");
    }
}
