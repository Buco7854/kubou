package com.kubou.domain.service;

import com.kubou.domain.entity.GameConfig;
import com.kubou.domain.entity.Question;
import com.kubou.domain.entity.UserAnswer;

public class SimpleScoringStrategy implements IScoringStrategy {

    private static final int CORRECT_ANSWER_SCORE = 1000;
    private static final int INCORRECT_ANSWER_SCORE = 0;

    @Override
    public int calculate(UserAnswer answer, Question question, GameConfig config, int currentStreak) {
        if (answer.getAnswerIndex() == question.getCorrectAnswerIndex()) {
            return CORRECT_ANSWER_SCORE;
        }
        return INCORRECT_ANSWER_SCORE;
    }
}
