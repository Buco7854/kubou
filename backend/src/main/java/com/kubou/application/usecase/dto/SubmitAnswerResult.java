package com.kubou.application.usecase.dto;

public class SubmitAnswerResult {
    private final int score;
    private final boolean roundFinished;
    private final boolean isCorrect;

    public SubmitAnswerResult(int score, boolean roundFinished, boolean isCorrect) {
        this.score = score;
        this.roundFinished = roundFinished;
        this.isCorrect = isCorrect;
    }

    public int getScore() {
        return score;
    }

    public boolean isRoundFinished() {
        return roundFinished;
    }

    public boolean isCorrect() {
        return isCorrect;
    }
}