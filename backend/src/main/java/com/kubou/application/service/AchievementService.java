package com.kubou.application.service;

import com.kubou.application.repository.PlayerAchievementRepository;
import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.application.repository.QuestionRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.domain.entity.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AchievementService {

    private final PlayerAchievementRepository achievementRepository;
    private final PlayerResponseRepository playerResponseRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public AchievementService(
            PlayerAchievementRepository achievementRepository,
            PlayerResponseRepository playerResponseRepository,
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.achievementRepository = achievementRepository;
        this.playerResponseRepository = playerResponseRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.messagingTemplate = messagingTemplate;
    }

    private boolean isGuest(String userId) {
        return userId != null && userId.startsWith("guest-");
    }

    @Transactional
    public void checkAndUnlockAchievements(Player player, UserAnswer userAnswer, boolean isCorrect) {
        String subjectId = player.getUserId() != null ? player.getUserId() : player.getId();
        
        if (isGuest(subjectId)) {
            return;
        }

        List<PlayerAchievement> existingAchievements = achievementRepository.findByPlayerId(subjectId);

        // SNIPER (5 correct in a row)
        if (isCorrect && player.getCurrentStreak() >= 5) {
            unlockIfNotExists(subjectId, AchievementType.SNIPER, existingAchievements);
        }

        // FLASH (Correct < 1s)
        if (isCorrect && userAnswer.getTimeToAnswerMs() < 1000) {
            unlockIfNotExists(subjectId, AchievementType.FLASH, existingAchievements);
        }

        // TURBO (Correct < 5s AND Streak >= 3)
        if (isCorrect && userAnswer.getTimeToAnswerMs() < 5000 && player.getCurrentStreak() >= 3) {
            unlockIfNotExists(subjectId, AchievementType.TURBO, existingAchievements);
        }

        // COMEBACK (Correct after being at 0 streak)
        if (isCorrect && player.getCurrentStreak() == 1 && player.getScore() > 100) { 
             unlockIfNotExists(subjectId, AchievementType.COMEBACK, existingAchievements);
        }

        // LUCKY (Correct on Difficulty 5)
        if (isCorrect) {
            questionRepository.findById(userAnswer.getQuestionId()).ifPresent(question -> {
                if (question.getDifficultyLevel() >= 5) {
                    unlockIfNotExists(subjectId, AchievementType.LUCKY, existingAchievements);
                }
            });
        }
    }

    // End Game Achievements DISABLED
    public void awardEndGameAchievements(GameSession session) {
        // No-op: Badges like WINNER, TOP_3, PERFECT, FIRST_GAME, MARATHON are disabled.
    }

    private void unlockIfNotExists(String subjectId, AchievementType type, List<PlayerAchievement> existing) {
        boolean alreadyUnlocked = existing.stream().anyMatch(a -> a.getType() == type);
        if (!alreadyUnlocked) {
            PlayerAchievement achievement = new PlayerAchievement(
                    UUID.randomUUID().toString(),
                    subjectId,
                    type,
                    LocalDateTime.now()
            );
            achievementRepository.save(achievement);
            
            try {
                messagingTemplate.convertAndSendToUser(
                        subjectId, 
                        "/queue/achievements", 
                        achievement
                );
            } catch (Exception e) {
                // Ignore WS errors
            }
            
            existing.add(achievement);
        }
    }
}
