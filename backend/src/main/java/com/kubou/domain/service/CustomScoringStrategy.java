package com.kubou.domain.service;

import com.kubou.domain.entity.GameConfig;
import com.kubou.domain.entity.Question;
import com.kubou.domain.entity.ScoringSettings;
import com.kubou.domain.entity.UserAnswer;

public class CustomScoringStrategy implements IScoringStrategy {

    @Override
    public int calculate(UserAnswer answer, Question question, GameConfig config, int currentStreak) {
        if (answer.getAnswerIndex() != question.getCorrectAnswerIndex()) {
            return 0;
        }

        ScoringSettings settings = config.getScoringSettings();
        int baseScore = settings.getBaseScore();
        double timeWeight = settings.getTimeWeight();
        double streakMultiplier = settings.getStreakMultiplier();
        
        // Calculate score based on time
        double maxTime = 30000.0;
        double timeTaken = Math.min(answer.getTimeToAnswerMs(), maxTime);
        
        double timeFactor = 1.0 - (timeTaken / maxTime) * timeWeight;
        
        int score = (int) (baseScore * timeFactor);
        
        // Apply streak multiplier
        // If streakMultiplier is 1.1 (10% bonus), and streak is 2, maybe 1.2x?
        // Or just simple: score * (streakMultiplier ^ streak) or score * (1 + (streak * (multiplier - 1)))
        // Let's use a simple linear bonus for now: 1 + (streak * (multiplier - 1))
        // If multiplier is 1.0, bonus is 0.
        
        if (currentStreak > 0 && streakMultiplier > 1.0) {
            double bonusFactor = 1.0 + (currentStreak * (streakMultiplier - 1.0));
            // Cap bonus? Maybe.
            score = (int) (score * bonusFactor);
        }
        
        return score;
    }
}
