package com.kubou.interface_adapter.controller;

import com.kubou.application.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get analytics for the current user")
    public ResponseEntity<Map<String, Object>> getMyAnalytics(
            @RequestParam(defaultValue = "week") String period,
            Principal principal
    ) {
        // Principal name is the ID in our JWT setup (subject)
        String playerId = principal.getName();
        // If guest, ID might be guest-UUID.
        
        return ResponseEntity.ok(analyticsService.getPlayerAnalytics(playerId, period));
    }
}
