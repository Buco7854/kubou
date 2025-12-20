package com.kubou.domain.entity;

public class UserAnswer {
    private String playerId;
    private String questionId;
    private int answerIndex;
    private long timeToAnswerMs;

    public UserAnswer(String playerId, String questionId, int answerIndex, long timeToAnswerMs) {
        this.playerId = playerId;
        this.questionId = questionId;
        this.answerIndex = answerIndex;
        this.timeToAnswerMs = timeToAnswerMs;
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

    public long getTimeToAnswerMs() {
        return timeToAnswerMs;
    }

    public void setTimeToAnswerMs(long timeToAnswerMs) {
        this.timeToAnswerMs = timeToAnswerMs;
    }
}
