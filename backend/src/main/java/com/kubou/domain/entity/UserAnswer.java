package com.kubou.domain.entity;

public class UserAnswer {
    private String playerId;
    private String questionId;
    private int answerIndex;
    private long timeElapsedMs;

    public UserAnswer(String playerId, String questionId, int answerIndex, long timeElapsedMs) {
        this.playerId = playerId;
        this.questionId = questionId;
        this.answerIndex = answerIndex;
        this.timeElapsedMs = timeElapsedMs;
    }

    // Getters and Setters

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public int getAnswerIndex() {
        return answerIndex;
    }

    public void setAnswerIndex(int answerIndex) {
        this.answerIndex = answerIndex;
    }

    public long getTimeElapsedMs() {
        return timeElapsedMs;
    }

    public void setTimeElapsedMs(long timeElapsedMs) {
        this.timeElapsedMs = timeElapsedMs;
    }
}
