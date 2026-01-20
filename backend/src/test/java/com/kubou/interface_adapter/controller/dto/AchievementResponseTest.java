package com.kubou.interface_adapter.controller.dto;

import com.kubou.domain.entity.AchievementType;
import com.kubou.domain.entity.PlayerAchievement;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AchievementResponseTest {

    @Test
    void testConstructorAndGetters() {
        PlayerAchievement achievement = new PlayerAchievement("1", "user", AchievementType.SNIPER, LocalDateTime.now());
        List<PlayerAchievement> list = List.of(achievement);
        String message = "Test Message";

        AchievementResponse response = new AchievementResponse(true, message, list);

        assertTrue(response.isGuest());
        assertEquals(message, response.getMessage());
        assertEquals(list, response.getAchievements());
    }

    @Test
    void testSetters() {
        AchievementResponse response = new AchievementResponse(false, null, Collections.emptyList());

        response.setGuest(true);
        response.setMessage("New Message");
        PlayerAchievement achievement = new PlayerAchievement("2", "user", AchievementType.FLASH, LocalDateTime.now());
        response.setAchievements(List.of(achievement));

        assertTrue(response.isGuest());
        assertEquals("New Message", response.getMessage());
        assertEquals(1, response.getAchievements().size());
        assertEquals(AchievementType.FLASH, response.getAchievements().get(0).getType());
    }
}
