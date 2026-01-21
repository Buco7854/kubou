package com.kubou.interface_adapter.controller;

import com.kubou.application.repository.PlayerAchievementRepository;
import com.kubou.domain.entity.AchievementType;
import com.kubou.domain.entity.PlayerAchievement;
import com.kubou.interface_adapter.controller.dto.AchievementDto;
import com.kubou.interface_adapter.controller.dto.AchievementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    @Operation(summary = "Get achievements status with guest info and locked/unlocked status")
    public AchievementResponse getAchievementStatus(Principal principal) {
        boolean isGuest = principal.getName().startsWith("guest-");
        
        if (isGuest) {
            return new AchievementResponse(
                true,
                "Vous êtes invité. Vous devez créer un compte pour avoir des badges.",
                Collections.emptyList()
            );
        }

        List<PlayerAchievement> unlockedAchievements = achievementRepository.findByPlayerId(principal.getName());
        
        List<AchievementDto> allAchievements = Arrays.stream(AchievementType.values())
                .map(type -> {
                    PlayerAchievement unlocked = unlockedAchievements.stream()
                            .filter(a -> a.getType() == type)
                            .findFirst()
                            .orElse(null);
                    
                    return new AchievementDto(
                            type,
                            type.getName(),
                            type.getDescription(),
                            unlocked != null,
                            unlocked != null ? unlocked.getUnlockedAt() : null
                    );
                })
                .collect(Collectors.toList());

        return new AchievementResponse(
            false,
            null,
            allAchievements
        );
    }
}