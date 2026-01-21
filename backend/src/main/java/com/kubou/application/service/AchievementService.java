package com.kubou.application.service;

import com.kubou.application.repository.PlayerAchievementRepository;
import com.kubou.application.repository.QuestionRepository;
import com.kubou.domain.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AchievementService {

    private static final Logger logger = LoggerFactory.getLogger(AchievementService.class);

    private final PlayerAchievementRepository achievementRepository;
    private final QuestionRepository questionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public AchievementService(
            PlayerAchievementRepository achievementRepository,
            QuestionRepository questionRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.achievementRepository = achievementRepository;
        this.questionRepository = questionRepository;
        this.messagingTemplate = messagingTemplate;
    }

    private boolean isGuest(String userId) {
        return userId != null && userId.startsWith("guest-");
    }

    @Transactional
    public void checkAndUnlockAchievements(Player player, UserAnswer userAnswer, boolean isCorrect) {
        // CRITIQUE : On utilise le userId (le compte utilisateur) pour la persistance.
        String subjectId = player.getUserId();
        
        if (subjectId == null) {
            // Fallback sur l'ID joueur si pas de compte lié (ne devrait pas arriver pour un user connecté)
            // Mais pour la page "Mes succès", seul le userId compte.
            logger.warn("Achievement check skipped or degraded: Player {} has no User ID linked.", player.getId());
            return;
        }
        
        if (isGuest(subjectId)) {
            return;
        }

        // On cherche les succès liés au COMPTE UTILISATEUR
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
        // No-op
    }

    private void unlockIfNotExists(String subjectId, AchievementType type, List<PlayerAchievement> existing) {
        boolean alreadyUnlocked = existing.stream().anyMatch(a -> a.getType() == type);
        if (!alreadyUnlocked) {
            logger.info("Unlocking achievement {} for user {}", type, subjectId);
            
            PlayerAchievement achievement = new PlayerAchievement(
                    UUID.randomUUID().toString(),
                    subjectId, // On sauvegarde bien avec l'ID du compte
                    type,
                    LocalDateTime.now()
            );
            achievementRepository.save(achievement);
            
            try {
                // On notifie l'utilisateur sur son canal privé
                messagingTemplate.convertAndSendToUser(
                        subjectId, 
                        "/queue/achievements", 
                        achievement
                );
            } catch (Exception e) {
                logger.error("Failed to send achievement notification", e);
            }
            
            existing.add(achievement);
        }
    }
}