package com.kubou.application.usecase;

import com.kubou.domain.entity.Player;
import com.kubou.infrastructure.repository.jpa.GameSessionJpaRepository;
import com.kubou.infrastructure.repository.jpa.mapper.GameSessionMapper;
import com.kubou.infrastructure.repository.jpa.model.GameSessionData;
import com.kubou.infrastructure.repository.jpa.model.PlayerData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

@Service
public class JoinGameUseCase {

    private final GameSessionJpaRepository gameSessionJpaRepository;
    private final GameSessionMapper gameSessionMapper;

    public JoinGameUseCase(GameSessionJpaRepository gameSessionJpaRepository, GameSessionMapper gameSessionMapper) {
        this.gameSessionJpaRepository = gameSessionJpaRepository;
        this.gameSessionMapper = gameSessionMapper;
    }

    @Transactional
    public void execute(String pin, Player player) {
        GameSessionData gameSessionData = gameSessionJpaRepository.findByPin(pin)
                .orElseThrow(() -> new RuntimeException("Game session not found"));

        // Check if player already exists
        boolean playerExists = gameSessionData.getPlayers().stream()
                .anyMatch(p -> p.getId().equals(player.getId()));

        if (!playerExists) {
            PlayerData newPlayerData = gameSessionMapper.toData(player);
            newPlayerData.setGameSession(gameSessionData);
            gameSessionData.getPlayers().add(newPlayerData);

            // Auto-assign to team if team mode is enabled
            if (gameSessionData.isTeamMode() && !gameSessionData.getTeams().isEmpty()) {
                gameSessionData.getTeams().stream()
                        .min(Comparator.comparingInt(t -> t.getPlayerIds().size()))
                        .ifPresent(smallestTeam -> smallestTeam.getPlayerIds().add(player.getId()));
            }
            
            // No explicit save needed; transaction commit will flush changes to managed entity
        }
    }
}
