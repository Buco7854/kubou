package com.kubou.infrastructure.repository.jpa.mapper;

import com.kubou.domain.entity.PlayerResponse;
import com.kubou.infrastructure.repository.jpa.model.PlayerResponseData;
import org.springframework.stereotype.Component;

@Component
public class PlayerResponseMapper {

    public PlayerResponseData toData(PlayerResponse response) {
        if (response == null) return null;
        PlayerResponseData data = new PlayerResponseData();
        data.setId(response.getId());
        data.setGameSessionId(response.getGameSessionId());
        data.setPlayerId(response.getPlayerId());
        data.setQuestionId(response.getQuestionId());
        data.setSelectedAnswerIndex(response.getSelectedAnswerIndex());
        data.setCorrect(response.isCorrect());
        data.setTimeTakenMs(response.getTimeTakenMs());
        data.setScoreAwarded(response.getScoreAwarded());
        data.setTimestamp(response.getTimestamp());
        return data;
    }

    public PlayerResponse toDomain(PlayerResponseData data) {
        if (data == null) return null;
        return new PlayerResponse(
                data.getId(),
                data.getGameSessionId(),
                data.getPlayerId(),
                data.getQuestionId(),
                data.getSelectedAnswerIndex(),
                data.isCorrect(),
                data.getTimeTakenMs(),
                data.getScoreAwarded(),
                data.getTimestamp()
        );
    }
}
