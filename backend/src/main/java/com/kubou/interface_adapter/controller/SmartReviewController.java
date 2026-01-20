package com.kubou.interface_adapter.controller;

import com.kubou.application.service.SmartReviewService;
import com.kubou.domain.entity.Question;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/smart-review")
public class SmartReviewController {

    private final SmartReviewService smartReviewService;

    public SmartReviewController(SmartReviewService smartReviewService) {
        this.smartReviewService = smartReviewService;
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<List<Question>> getCatchUpQuiz(@PathVariable String playerId) {
        List<Question> catchUpQuiz = smartReviewService.createCatchUpQuiz(playerId);
        if (catchUpQuiz.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(catchUpQuiz);
    }
}
