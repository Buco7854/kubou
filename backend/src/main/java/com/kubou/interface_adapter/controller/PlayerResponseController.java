package com.kubou.interface_adapter.controller;

import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.application.repository.QuestionRepository;
import com.kubou.domain.entity.PlayerResponse;
import com.kubou.domain.entity.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/responses")
public class PlayerResponseController {

    private static final Logger logger = LoggerFactory.getLogger(PlayerResponseController.class);

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
        String playerId = principal.getName();
        logger.info("Submitting response for player ID: '{}', question ID: '{}'", playerId, req.questionId());

        Question q = questionRepository.findById(req.questionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        boolean correct = req.selectedAnswerIndex() == q.getCorrectAnswerIndex();
        logger.info("Answer is correct: {}", correct);

        PlayerResponse pr = new PlayerResponse(
                UUID.randomUUID().toString(),
                req.gameSessionId(),
                playerId,
                req.questionId(),
                req.selectedAnswerIndex(),
                correct,
                req.timeTakenMs(),
                0,
                LocalDateTime.now()
        );

        try {
            responseRepository.save(pr);
            logger.info("Successfully saved response for player ID: '{}'", playerId);
        } catch (Exception e) {
            logger.error("Failed to save response for player ID: '{}'", playerId, e);
            // Optionally re-throw or return an error response
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(pr);
    }
}
