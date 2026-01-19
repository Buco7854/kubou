package com.kubou.application.service;

import com.kubou.application.repository.PlayerAchievementRepository;
import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.domain.entity.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AchievementService {

    private final PlayerAchievementRepository achievementRepository;
    private final PlayerResponseRepository playerResponseRepository;
    private final QuizRepository quizRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public AchievementService(
            PlayerAchievementRepository achievementRepository,
            PlayerResponseRepository playerResponseRepository,
            QuizRepository quizRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.achievementRepository = achievementRepository;
        this.playerResponseRepository = playerResponseRepository;
        this.quizRepository = quizRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void checkAndUnlockAchievements(Player player, UserAnswer userAnswer, boolean isCorrect) {
        // FIX: Use stable userId (principal name) instead of session-scoped playerId
        String subjectId = player.getUserId() != null ? player.getUserId() : player.getId();
        List<PlayerAchievement> existingAchievements = achievementRepository.findByPlayerId(subjectId);

        // Check SNIPER (5 correct in a row)
        if (isCorrect && player.getCurrentStreak() >= 5) {
            unlockIfNotExists(subjectId, AchievementType.SNIPER, existingAchievements);
        }

        // Check FLASH (Correct answer < 1s)
        if (isCorrect && userAnswer.getTimeToAnswerMs() < 1000) {
            unlockIfNotExists(subjectId, AchievementType.FLASH, existingAchievements);
        }
    }

    @Transactional
    public void awardEndGameAchievements(GameSession session) {
        System.out.println("Awarding End Game Achievements for session: " + session.getId());
        
        // 1. Get Quiz to know total questions count
        Quiz quiz = quizRepository.findById(session.getQuizId()).orElse(null);
        if (quiz == null) {
            System.out.println("Quiz not found for session");
            return;
        }
        int totalQuestions = quiz.getQuestions().size();

        // 2. Get all responses for this session to check PERFECT
        List<PlayerResponse> allResponses = playerResponseRepository.findByGameSessionId(session.getId());
        Map<String, List<PlayerResponse>> responsesByPlayerId = allResponses.stream()
                .collect(Collectors.groupingBy(PlayerResponse::getPlayerId));

        // 3. Sort players by score for WINNER / TOP_3
        List<Player> sortedPlayers = session.getPlayers().stream()
                .sorted(Comparator.comparingInt(Player::getScore).reversed())
                .collect(Collectors.toList());

        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player player = sortedPlayers.get(i);
            String subjectId = player.getUserId() != null ? player.getUserId() : player.getId();
            
            System.out.println("Processing player: " + player.getNickname() + " (SubjectID: " + subjectId + ")");
            
            // Fetch existing achievements for this user (stable ID)
            List<PlayerAchievement> existing = achievementRepository.findByPlayerId(subjectId);

            // FIRST_GAME
            unlockIfNotExists(subjectId, AchievementType.FIRST_GAME, existing);

            // WINNER (Rank 1)
            if (i == 0) {
                unlockIfNotExists(subjectId, AchievementType.WINNER, existing);
            }

            // TOP_3 (Rank 1, 2, 3)
            if (i < 3) {
                unlockIfNotExists(subjectId, AchievementType.TOP_3, existing);
            }

            // PERFECT (100% correct)
            // Map session player ID to responses
            List<PlayerResponse> playerResponses = responsesByPlayerId.get(player.getId());
            if (playerResponses != null && !playerResponses.isEmpty()) {
                long correctCount = playerResponses.stream().filter(PlayerResponse::isCorrect).count();
                System.out.println("Player " + player.getNickname() + " correct answers: " + correctCount + "/" + totalQuestions);
                if (correctCount == totalQuestions && totalQuestions > 0) {
                    unlockIfNotExists(subjectId, AchievementType.PERFECT, existing);
                }
            } else {
                System.out.println("No responses found for player " + player.getId());
            }
        }
    }

    private void unlockIfNotExists(String subjectId, AchievementType type, List<PlayerAchievement> existing) {
        boolean alreadyUnlocked = existing.stream().anyMatch(a -> a.getType() == type);
        if (!alreadyUnlocked) {
            System.out.println("Unlocking achievement " + type + " for " + subjectId);
            PlayerAchievement achievement = new PlayerAchievement(
                    UUID.randomUUID().toString(),
                    subjectId,
                    type,
                    LocalDateTime.now()
            );
            achievementRepository.save(achievement);
            
            // Notify user via WebSocket using the stable subjectId (principal name)
            messagingTemplate.convertAndSendToUser(
                    subjectId, 
                    "/queue/achievements", 
                    achievement
            );
            
            // Add to local list to prevent double insertion in same transaction loop
            existing.add(achievement);
        } else {
            System.out.println("Achievement " + type + " already unlocked for " + subjectId);
        }
    }
}
