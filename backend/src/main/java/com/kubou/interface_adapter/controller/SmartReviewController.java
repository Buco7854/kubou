package com.kubou.interface_adapter.controller;

import com.kubou.application.service.SmartReviewService;
import com.kubou.domain.entity.Quiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/smart-review")
@SecurityRequirement(name = "bearerAuth")
public class SmartReviewController {

    private final SmartReviewService smartReviewService;

    public SmartReviewController(SmartReviewService smartReviewService) {
        this.smartReviewService = smartReviewService;
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate a review quiz based on past mistakes")
    public ResponseEntity<Quiz> generateReview(Principal principal) {
        try {
            Quiz quiz = smartReviewService.generateReviewQuiz(principal.getName());
            return ResponseEntity.ok(quiz);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build(); // Or return specific error message
        }
    }
}
