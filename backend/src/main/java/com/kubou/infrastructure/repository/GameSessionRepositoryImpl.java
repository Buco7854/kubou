package com.kubou.infrastructure.repository;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.domain.entity.GameSession;
import com.kubou.domain.entity.Player;
import com.kubou.domain.entity.ScoringSettings;
import com.kubou.domain.entity.Team;
import com.kubou.infrastructure.repository.jpa.GameSessionJpaRepository;
import com.kubou.infrastructure.repository.jpa.mapper.GameSessionMapper;
import com.kubou.infrastructure.repository.jpa.model.GameSessionData;
import com.kubou.infrastructure.repository.jpa.model.PlayerData;
import com.kubou.infrastructure.repository.jpa.model.TeamData;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("jpaGameSessionRepository")
public class GameSessionRepositoryImpl implements GameSessionRepository {

    private final GameSessionJpaRepository gameSessionJpaRepository;
    private final GameSessionMapper gameSessionMapper;

    public GameSessionRepositoryImpl(GameSessionJpaRepository gameSessionJpaRepository, GameSessionMapper gameSessionMapper) {
        this.gameSessionJpaRepository = gameSessionJpaRepository;
        this.gameSessionMapper = gameSessionMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GameSession> findById(String id) {
        return gameSessionJpaRepository.findById(id).map(gameSessionMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GameSession> findByPin(String pin) {
        return gameSessionJpaRepository.findByPin(pin).map(gameSessionMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findByHostId(String hostId) {
        return gameSessionJpaRepository.findByHostId(hostId).stream()
                .map(gameSessionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findActiveSessionsByPlayerUserId(String userId) {
        return gameSessionJpaRepository.findActiveSessionsByPlayerUserId(userId).stream()
                .map(gameSessionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void save(GameSession gameSession) {
        // To avoid "Multiple representations" error, we should fetch the existing entity and update it
        // instead of trying to save a completely new detached object tree.
        Optional<GameSessionData> existingDataOpt = gameSessionJpaRepository.findById(gameSession.getId());

        if (existingDataOpt.isPresent()) {
            GameSessionData existingData = existingDataOpt.get();
            updateDataFromDomain(existingData, gameSession);
            gameSessionJpaRepository.save(existingData);
        } else {
            // New entity
            GameSessionData newData = gameSessionMapper.toData(gameSession);
            gameSessionJpaRepository.save(newData);
        }
    }

    private void updateDataFromDomain(GameSessionData data, GameSession domain) {
        data.setPin(domain.getPin());
        data.setQuizId(domain.getQuizId());
        data.setHostId(domain.getHostId());
        data.setCurrentQuestionIndex(domain.getCurrentQuestionIndex());
        data.setState(domain.getState());
        data.setTeamMode(domain.isTeamMode());

        if (domain.getGameConfig() != null && domain.getGameConfig().getScoringSettings() != null) {
            ScoringSettings settings = domain.getGameConfig().getScoringSettings();
            data.setBaseScore(settings.getBaseScore());
            data.setTimeWeight(settings.getTimeWeight());
            data.setStreakMultiplier(settings.getStreakMultiplier());
        }

        // Update Players list carefully
        Map<String, PlayerData> existingPlayersMap = data.getPlayers().stream()
                .collect(Collectors.toMap(PlayerData::getId, Function.identity()));

        List<PlayerData> updatedPlayers = new ArrayList<>();
        
        for (Player domainPlayer : domain.getPlayers()) {
            PlayerData playerData = existingPlayersMap.get(domainPlayer.getId());
            if (playerData == null) {
                // New player
                playerData = gameSessionMapper.toData(domainPlayer);
                playerData.setGameSession(data);
            } else {
                // Update existing player
                playerData.setUserId(domainPlayer.getUserId());
                playerData.setNickname(domainPlayer.getNickname());
                playerData.setScore(domainPlayer.getScore());
                playerData.setCurrentStreak(domainPlayer.getCurrentStreak());
            }
            updatedPlayers.add(playerData);
        }
        
        // Replace the list
        data.getPlayers().clear();
        data.getPlayers().addAll(updatedPlayers);
        
        // Update Teams list carefully (similar logic)
        if (data.getTeams() == null) {
            data.setTeams(new ArrayList<>());
        }
        
        Map<String, TeamData> existingTeamsMap = data.getTeams().stream()
                .collect(Collectors.toMap(TeamData::getId, Function.identity()));
        
        List<TeamData> updatedTeams = new ArrayList<>();
        
        for (Team domainTeam : domain.getTeams()) {
            TeamData teamData = existingTeamsMap.get(domainTeam.getId());
            if (teamData == null) {
                teamData = gameSessionMapper.toData(domainTeam);
            } else {
                teamData.setName(domainTeam.getName());
                teamData.setScore(domainTeam.getScore());
                teamData.setPlayerIds(domainTeam.getPlayerIds());
            }
            updatedTeams.add(teamData);
        }
        
        data.getTeams().clear();
        data.getTeams().addAll(updatedTeams);
    }
}
