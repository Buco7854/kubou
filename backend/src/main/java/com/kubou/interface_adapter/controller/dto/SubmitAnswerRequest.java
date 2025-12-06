package com.kubou.interface_adapter.controller.dto;

public class SubmitAnswerRequest {
    private String questionId;
    private int answerIndex;
    private long timeToAnswerMs;

    // Getters and Setters
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
