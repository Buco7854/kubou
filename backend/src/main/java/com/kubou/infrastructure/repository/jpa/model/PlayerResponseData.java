package com.kubou.infrastructure.repository.jpa.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_responses")
public class PlayerResponseData {
    @Id
    private String id;
    private String gameSessionId;
    private String playerId;
    private String questionId;
    private int selectedAnswerIndex;
    private boolean isCorrect;
    private long timeTakenMs;
    private int scoreAwarded;
    private LocalDateTime timestamp;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGameSessionId() { return gameSessionId; }
    public void setGameSessionId(String gameSessionId) { this.gameSessionId = gameSessionId; }
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }
    public int getSelectedAnswerIndex() { return selectedAnswerIndex; }
    public void setSelectedAnswerIndex(int selectedAnswerIndex) { this.selectedAnswerIndex = selectedAnswerIndex; }
    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }
    public long getTimeTakenMs() { return timeTakenMs; }
    public void setTimeTakenMs(long timeTakenMs) { this.timeTakenMs = timeTakenMs; }
    public int getScoreAwarded() { return scoreAwarded; }
    public void setScoreAwarded(int scoreAwarded) { this.scoreAwarded = scoreAwarded; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
