package com.kubou.interface_adapter.controller;

import com.kubou.application.repository.PlayerAchievementRepository;
import com.kubou.domain.entity.PlayerAchievement;
import com.kubou.interface_adapter.controller.dto.AchievementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/achievements")
@SecurityRequirement(name = "bearerAuth")
public class AchievementController {

    private final PlayerAchievementRepository achievementRepository;

    public AchievementController(PlayerAchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    @GetMapping("/me")
    @Operation(summary = "Get achievements for the current user (Legacy List format)")
    public List<PlayerAchievement> getMyAchievements(Principal principal) {
        if (principal.getName().startsWith("guest-")) {
            return Collections.emptyList();
        }
        return achievementRepository.findByPlayerId(principal.getName());
    }

    @GetMapping("/status")
    @Operation(summary = "Get achievements status with guest info")
    public AchievementResponse getAchievementStatus(Principal principal) {
        boolean isGuest = principal.getName().startsWith("guest-");
        
        if (isGuest) {
            return new AchievementResponse(
                true,
                "Vous êtes invité. Vous devez créer un compte pour avoir des badges.",
                Collections.emptyList()
            );
        }

        List<PlayerAchievement> achievements = achievementRepository.findByPlayerId(principal.getName());
        return new AchievementResponse(
            false,
            null,
            achievements
        );
    }

    // DEBUG ENDPOINT - TO BE REMOVED LATER
    @GetMapping("/debug")
    @Operation(summary = "DEBUG: List all achievements in DB")
    public List<PlayerAchievement> debugAllAchievements() {
        return achievementRepository.findAll();
    }
}
