package com.kubou.application.service;

import com.kubou.application.repository.PlayerAchievementRepository;
import com.kubou.domain.entity.AchievementType;
import com.kubou.domain.entity.Player;
import com.kubou.domain.entity.PlayerAchievement;
import com.kubou.domain.entity.UserAnswer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AchievementService {

    private final PlayerAchievementRepository achievementRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public AchievementService(PlayerAchievementRepository achievementRepository, SimpMessagingTemplate messagingTemplate) {
        this.achievementRepository = achievementRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void checkAndUnlockAchievements(Player player, UserAnswer userAnswer, boolean isCorrect) {
        List<PlayerAchievement> existingAchievements = achievementRepository.findByPlayerId(player.getId());

        // Check SNIPER (5 correct in a row)
        if (isCorrect && player.getCurrentStreak() >= 5) {
            unlockIfNotExists(player, AchievementType.SNIPER, existingAchievements);
        }

        // Check FLASH (Correct answer < 1s)
        if (isCorrect && userAnswer.getTimeToAnswerMs() < 1000) {
            unlockIfNotExists(player, AchievementType.FLASH, existingAchievements);
        }
    }

    private void unlockIfNotExists(Player player, AchievementType type, List<PlayerAchievement> existing) {
        boolean alreadyUnlocked = existing.stream().anyMatch(a -> a.getType() == type);
        if (!alreadyUnlocked) {
            PlayerAchievement achievement = new PlayerAchievement(
                    UUID.randomUUID().toString(),
                    player.getId(),
                    type,
                    LocalDateTime.now()
            );
            achievementRepository.save(achievement);
            
            // Notify user via WebSocket
            // Assuming we can target user by username/principal name. 
            // Player ID might not be the principal name depending on how we map players.
            // In GameController joinLobby, we used principal.getName() as ID.
            messagingTemplate.convertAndSendToUser(
                    player.getId(), 
                    "/queue/achievements", 
                    achievement
            );
        }
    }
}
