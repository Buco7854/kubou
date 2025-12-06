package com.kubou.infrastructure.repository.jpa.mapper;

import com.kubou.domain.entity.GameSession;
import com.kubou.domain.entity.Player;
import com.kubou.infrastructure.repository.jpa.model.GameSessionData;
import com.kubou.infrastructure.repository.jpa.model.PlayerData;
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
        if (session.getPlayers() != null) {
            data.setPlayers(session.getPlayers().stream().map(this::toData).collect(Collectors.toList()));
            data.getPlayers().forEach(p -> p.setGameSession(data));
        }
        return data;
    }

    public PlayerData toData(Player player) {
        if (player == null) {
            return null;
        }
        PlayerData data = new PlayerData();
        data.setId(player.getId());
        data.setNickname(player.getNickname());
        data.setScore(player.getScore());
        return data;
    }

    public GameSession toDomain(GameSessionData data) {
        if (data == null) {
            return null;
        }
        GameSession session = new GameSession(data.getId(), data.getPin(), data.getQuizId(), data.getHostId());
        session.setCurrentQuestionIndex(data.getCurrentQuestionIndex());
        session.setState(data.getState());
        session.setPlayers(data.getPlayers().stream().map(this::toDomain).collect(Collectors.toList()));
        return session;
    }

    public Player toDomain(PlayerData data) {
        if (data == null) {
            return null;
        }
        Player player = new Player(data.getId(), data.getNickname());
        player.setScore(data.getScore());
        return player;
    }
}
