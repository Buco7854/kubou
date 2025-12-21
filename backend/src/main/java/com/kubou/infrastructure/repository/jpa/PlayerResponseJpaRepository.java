package com.kubou.infrastructure.repository.jpa;

import com.kubou.infrastructure.repository.jpa.model.PlayerResponseData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlayerResponseJpaRepository extends JpaRepository<PlayerResponseData, String> {
    List<PlayerResponseData> findByGameSessionId(String gameSessionId);
    List<PlayerResponseData> findByGameSessionIdAndQuestionId(String gameSessionId, String questionId);
    List<PlayerResponseData> findByPlayerId(String playerId);
    List<PlayerResponseData> findByPlayerIdAndTimestampBetween(String playerId, LocalDateTime start, LocalDateTime end);
}
