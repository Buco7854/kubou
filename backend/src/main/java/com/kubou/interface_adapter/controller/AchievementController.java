package com.kubou.interface_adapter.controller;

import com.kubou.application.repository.PlayerAchievementRepository;
import com.kubou.domain.entity.PlayerAchievement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
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
    @Operation(summary = "Get achievements for the current user")
    public List<PlayerAchievement> getMyAchievements(Principal principal) {
        // Note: principal.getName() returns the user ID (subject) from JWT
        return achievementRepository.findByPlayerId(principal.getName());
    }
}
