package com.kubou.application.service;

import com.kubou.application.repository.PlayerAchievementRepository;
import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.application.repository.QuestionRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.domain.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock
    private PlayerAchievementRepository achievementRepository;
    @Mock
    private PlayerResponseRepository playerResponseRepository;
    @Mock
    private QuizRepository quizRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AchievementService achievementService;

    @Test
    void checkAndUnlockAchievements_ShouldUnlockSniper_WhenStreakIs5() {
        Player player = new Player("p1", "user1", "Nick");
        player.setCurrentStreak(5);
        // Use time > 5000 to avoid TURBO badge
        UserAnswer answer = new UserAnswer("p1", "q1", 0, 6000);

        when(achievementRepository.findByPlayerId("user1")).thenReturn(new ArrayList<>());

        achievementService.checkAndUnlockAchievements(player, answer, true);

        verify(achievementRepository, times(1)).save(any(PlayerAchievement.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq("user1"), eq("/queue/achievements"), any(PlayerAchievement.class));
    }

    @Test
    void checkAndUnlockAchievements_ShouldNotUnlock_WhenAlreadyExists() {
        Player player = new Player("p1", "user1", "Nick");
        player.setCurrentStreak(5);
        UserAnswer answer = new UserAnswer("p1", "q1", 0, 6000);

        PlayerAchievement existing = new PlayerAchievement("1", "user1", AchievementType.SNIPER, LocalDateTime.now());
        List<PlayerAchievement> list = new ArrayList<>();
        list.add(existing);

        when(achievementRepository.findByPlayerId("user1")).thenReturn(list);

        achievementService.checkAndUnlockAchievements(player, answer, true);

        verify(achievementRepository, never()).save(any(PlayerAchievement.class));
    }

    @Test
    void checkAndUnlockAchievements_ShouldUnlockFlash_WhenTimeIsUnder1s() {
        Player player = new Player("p1", "user1", "Nick");
        UserAnswer answer = new UserAnswer("p1", "q1", 0, 500); // 500ms

        when(achievementRepository.findByPlayerId("user1")).thenReturn(new ArrayList<>());

        achievementService.checkAndUnlockAchievements(player, answer, true);

        verify(achievementRepository, times(1)).save(any(PlayerAchievement.class));
    }

    @Test
    void checkAndUnlockAchievements_ShouldUnlockTurbo_WhenStreak3AndFast() {
        Player player = new Player("p1", "user1", "Nick");
        player.setCurrentStreak(3);
        UserAnswer answer = new UserAnswer("p1", "q1", 0, 4000); // < 5s

        when(achievementRepository.findByPlayerId("user1")).thenReturn(new ArrayList<>());

        achievementService.checkAndUnlockAchievements(player, answer, true);

        verify(achievementRepository, atLeastOnce()).save(any(PlayerAchievement.class));
    }

    @Test
    void checkAndUnlockAchievements_ShouldUnlockComeback_WhenStreak1AndScorePositive() {
        Player player = new Player("p1", "user1", "Nick");
        player.setCurrentStreak(1);
        player.setScore(200);
        UserAnswer answer = new UserAnswer("p1", "q1", 0, 6000);

        when(achievementRepository.findByPlayerId("user1")).thenReturn(new ArrayList<>());

        achievementService.checkAndUnlockAchievements(player, answer, true);

        verify(achievementRepository, atLeastOnce()).save(any(PlayerAchievement.class));
    }

    @Test
    void checkAndUnlockAchievements_ShouldUnlockLucky_WhenDifficulty5() {
        Player player = new Player("p1", "user1", "Nick");
        UserAnswer answer = new UserAnswer("p1", "q1", 0, 6000);
        Question question = new Question("q1", "Hard Q", Collections.emptyList(), 0, Collections.emptyList(), 5);

        when(achievementRepository.findByPlayerId("user1")).thenReturn(new ArrayList<>());
        when(questionRepository.findById("q1")).thenReturn(Optional.of(question));

        achievementService.checkAndUnlockAchievements(player, answer, true);

        verify(achievementRepository, atLeastOnce()).save(any(PlayerAchievement.class));
    }

    @Test
    void checkAndUnlockAchievements_ShouldSkip_WhenGuest() {
        Player player = new Player("p1", "guest-123", "Guest");
        UserAnswer answer = new UserAnswer("p1", "q1", 0, 500);

        achievementService.checkAndUnlockAchievements(player, answer, true);

        verify(achievementRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    void awardEndGameAchievements_ShouldDoNothing_AsDisabled() {
        GameSession session = new GameSession("g1", "123456", "q1", "host");
        achievementService.awardEndGameAchievements(session);
        verifyNoInteractions(achievementRepository);
    }
}
