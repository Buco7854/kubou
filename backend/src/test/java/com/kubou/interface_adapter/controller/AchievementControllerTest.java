package com.kubou.interface_adapter.controller;

import com.kubou.application.repository.PlayerAchievementRepository;
import com.kubou.domain.entity.AchievementType;
import com.kubou.domain.entity.PlayerAchievement;
import com.kubou.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AchievementController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simplicity in unit test
class AchievementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerAchievementRepository achievementRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider; // Mocked to satisfy dependency injection

    @Test
    void getMyAchievements_ShouldReturnList_WhenUserIsAuthenticated() throws Exception {
        // Use SNIPER instead of FIRST_GAME as it is a valid enum value
        PlayerAchievement achievement = new PlayerAchievement("1", "testuser", AchievementType.SNIPER, LocalDateTime.now());
        when(achievementRepository.findByPlayerId("testuser")).thenReturn(List.of(achievement));

        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("testuser");

        mockMvc.perform(get("/api/v1/achievements/me")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].type").value("SNIPER"));
    }

    @Test
    void getMyAchievements_ShouldReturnEmptyList_WhenUserIsGuest() throws Exception {
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("guest-123");

        mockMvc.perform(get("/api/v1/achievements/me")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAchievementStatus_ShouldReturnStatus_WhenUserIsAuthenticated() throws Exception {
        // Use FLASH instead of WINNER as it is a valid enum value
        PlayerAchievement achievement = new PlayerAchievement("1", "testuser", AchievementType.FLASH, LocalDateTime.now());
        when(achievementRepository.findByPlayerId("testuser")).thenReturn(List.of(achievement));

        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("testuser");

        mockMvc.perform(get("/api/v1/achievements/status")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guest").value(false))
                .andExpect(jsonPath("$.achievements").isArray())
                .andExpect(jsonPath("$.achievements[?(@.type == 'FLASH')].unlocked").value(true))
                .andExpect(jsonPath("$.achievements[?(@.type == 'SNIPER')].unlocked").value(false));
    }

    @Test
    void getAchievementStatus_ShouldReturnGuestStatus_WhenUserIsGuest() throws Exception {
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("guest-123");

        mockMvc.perform(get("/api/v1/achievements/status")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guest").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.achievements").isEmpty());
    }
}