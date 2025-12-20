package com.kubou.infrastructure.repository.jpa.mapper;

import com.kubou.domain.entity.GameConfig;
import com.kubou.domain.entity.GameSession;
import com.kubou.domain.entity.Player;
import com.kubou.domain.entity.ScoringSettings;
import com.kubou.domain.entity.Team;
import com.kubou.infrastructure.repository.jpa.model.GameSessionData;
import com.kubou.infrastructure.repository.jpa.model.PlayerData;
import com.kubou.infrastructure.repository.jpa.model.TeamData;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class GameSessionMapper {

    public GameSessionData toData(GameSession session) {
        if (session == null) {
            return null;
        }
        GameSessionData data = new GameSessionData();
        data.setId(session.getId());
        data.setPin(session.getPin());
        data.setQuizId(session.getQuizId());
        data.setHostId(session.getHostId());
        data.setCurrentQuestionIndex(session.getCurrentQuestionIndex());
        data.setState(session.getState());
        data.setTeamMode(session.isTeamMode());
        
        // Map Scoring Settings
        if (session.getGameConfig() != null && session.getGameConfig().getScoringSettings() != null) {
            ScoringSettings settings = session.getGameConfig().getScoringSettings();
            data.setBaseScore(settings.getBaseScore());
            data.setTimeWeight(settings.getTimeWeight());
            data.setStreakMultiplier(settings.getStreakMultiplier());
        }

        if (session.getPlayers() != null) {
            data.setPlayers(session.getPlayers().stream().map(this::toData).collect(Collectors.toList()));
            data.getPlayers().forEach(p -> p.setGameSession(data));
        }
        
        if (session.getTeams() != null) {
            data.setTeams(session.getTeams().stream().map(this::toData).collect(Collectors.toList()));
        }
        
        return data;
    }

    public PlayerData toData(Player player) {
        if (player == null) {
            return null;
        }
        PlayerData data = new PlayerData();
        data.setId(player.getId());
        data.setUserId(player.getUserId());
        data.setNickname(player.getNickname());
        data.setScore(player.getScore());
        data.setCurrentStreak(player.getCurrentStreak());
        return data;
    }
    
    public TeamData toData(Team team) {
        if (team == null) return null;
        TeamData data = new TeamData();
        data.setId(team.getId());
        data.setName(team.getName());
        data.setScore(team.getScore());
        data.setPlayerIds(team.getPlayerIds());
        return data;
    }

    public GameSession toDomain(GameSessionData data) {
        if (data == null) {
            return null;
        }
        GameSession session = new GameSession(data.getId(), data.getPin(), data.getQuizId(), data.getHostId());
        session.setCurrentQuestionIndex(data.getCurrentQuestionIndex());
        session.setState(data.getState());
        session.setTeamMode(data.isTeamMode());
        
        // Map Scoring Settings back
        ScoringSettings settings = new ScoringSettings(data.getBaseScore(), data.getTimeWeight(), data.getStreakMultiplier());
        session.setGameConfig(new GameConfig(settings));

        if (data.getPlayers() != null) {
            session.setPlayers(data.getPlayers().stream().map(this::toDomain).collect(Collectors.toList()));
        }
        
        if (data.getTeams() != null) {
            session.setTeams(data.getTeams().stream().map(this::toDomain).collect(Collectors.toList()));
        }
        
        return session;
    }

    public Player toDomain(PlayerData data) {
        if (data == null) {
            return null;
        }
        Player player = new Player(data.getId(), data.getUserId(), data.getNickname());
        player.setScore(data.getScore());
        player.setCurrentStreak(data.getCurrentStreak());
        return player;
    }
    
    public Team toDomain(TeamData data) {
        if (data == null) return null;
        Team team = new Team(data.getId(), data.getName());
        team.setScore(data.getScore());
        team.setPlayerIds(data.getPlayerIds());
        return team;
    }
}
