package com.kubou.interface_adapter.controller;

import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.application.repository.QuestionRepository;
import com.kubou.domain.entity.PlayerResponse;
import com.kubou.domain.entity.Question;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/responses")
public class PlayerResponseController {

    private final PlayerResponseRepository responseRepository;
    private final QuestionRepository questionRepository;

    public PlayerResponseController(PlayerResponseRepository responseRepository,
                                    QuestionRepository questionRepository) {
        this.responseRepository = responseRepository;
        this.questionRepository = questionRepository;
    }

    public record SubmitResponseRequest(
            String gameSessionId,
            String questionId,
            int selectedAnswerIndex,
            long timeTakenMs
    ) {}

    @PostMapping
    public ResponseEntity<PlayerResponse> submit(Principal principal, @RequestBody SubmitResponseRequest req) {
        Question q = questionRepository.findById(req.questionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        boolean correct = req.selectedAnswerIndex() == q.getCorrectAnswerIndex();

        PlayerResponse pr = new PlayerResponse(
                UUID.randomUUID().toString(),
                req.gameSessionId(),
                principal.getName(),
                req.questionId(),
                req.selectedAnswerIndex(),
                correct,
                req.timeTakenMs(),
                0,
                LocalDateTime.now()
        );

        responseRepository.save(pr);
        return ResponseEntity.ok(pr);
    }
}
