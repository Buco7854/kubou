package com.kubou.domain.entity;

import java.time.LocalDateTime;

public class PlayerResponse {
    private String id;
    private String gameSessionId;
    private String playerId;
    private String questionId;
    private int selectedAnswerIndex;
    private boolean isCorrect;
    private long timeTakenMs;
    private int scoreAwarded;
    private LocalDateTime timestamp;

    public PlayerResponse(String id, String gameSessionId, String playerId, String questionId, int selectedAnswerIndex, boolean isCorrect, long timeTakenMs, int scoreAwarded, LocalDateTime timestamp) {
        this.id = id;
        this.gameSessionId = gameSessionId;
        this.playerId = playerId;
        this.questionId = questionId;
        this.selectedAnswerIndex = selectedAnswerIndex;
        this.isCorrect = isCorrect;
        this.timeTakenMs = timeTakenMs;
        this.scoreAwarded = scoreAwarded;
        this.timestamp = timestamp;
    }

    // Getters
    public String getId() { return id; }
    public String getGameSessionId() { return gameSessionId; }
    public String getPlayerId() { return playerId; }
    public String getQuestionId() { return questionId; }
    public int getSelectedAnswerIndex() { return selectedAnswerIndex; }
    public boolean isCorrect() { return isCorrect; }
    public long getTimeTakenMs() { return timeTakenMs; }
    public int getScoreAwarded() { return scoreAwarded; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
