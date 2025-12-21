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

        // Check if player already exists based on userId (which is the principal name)
        // We check if any player in the session has the same userId as the incoming player
        boolean playerAlreadyInSession = gameSessionData.getPlayers().stream()
                .anyMatch(p -> p.getUserId() != null && p.getUserId().equals(player.getUserId()));

        if (!playerAlreadyInSession) {
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
        } else {
            // Optional: Update existing player info (e.g. nickname) if needed, or just ignore
            // For now, we just don't add a duplicate entry.
        }
    }
}
