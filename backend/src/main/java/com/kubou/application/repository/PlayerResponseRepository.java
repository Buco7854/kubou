package com.kubou.application.repository;

import com.kubou.domain.entity.PlayerResponse;
import java.time.LocalDateTime;
import java.util.List;

public interface PlayerResponseRepository {
    void save(PlayerResponse response);
    List<PlayerResponse> findByGameSessionId(String gameSessionId);
    List<PlayerResponse> findByGameSessionIdAndQuestionId(String gameSessionId, String questionId);
    List<PlayerResponse> findByPlayerId(String playerId);
    List<PlayerResponse> findByPlayerIdAndDateRange(String playerId, LocalDateTime start, LocalDateTime end);
}
