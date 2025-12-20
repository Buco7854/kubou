package com.kubou.domain.service;

import com.kubou.domain.entity.GameConfig;
import com.kubou.domain.entity.Question;
import com.kubou.domain.entity.UserAnswer;

public class WeightedScoringStrategy implements IScoringStrategy {

    private static final int BASE_SCORE = 1000;

    @Override
    public int calculate(UserAnswer answer, Question question, GameConfig config, int currentStreak) {
        if (answer.getAnswerIndex() != question.getCorrectAnswerIndex()) {
            return 0;
        }

        double maxTime = 30000.0;
        double timeTaken = Math.min(answer.getTimeToAnswerMs(), maxTime);
        
        double timeFactor = 1.0 - (timeTaken / maxTime) / 2.0;
        
        return (int) (BASE_SCORE * timeFactor);
    }
}
