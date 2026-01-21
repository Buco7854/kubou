package com.kubou.interface_adapter.controller;

import com.kubou.application.service.SmartReviewService;
import com.kubou.domain.entity.Question;
import com.kubou.interface_adapter.controller.dto.QuestionDTO;
import com.kubou.interface_adapter.controller.dto.QuizDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/smart-review")
public class SmartReviewController {

    private static final Logger logger = LoggerFactory.getLogger(SmartReviewController.class);

    private final SmartReviewService smartReviewService;

    public SmartReviewController(SmartReviewService smartReviewService) {
        this.smartReviewService = smartReviewService;
    }

    @PostMapping("/generate")
    public ResponseEntity<QuizDTO> generateSmartReview(Principal principal) {
        String playerId = principal.getName();
        logger.info("Generating smart review for player ID: '{}'", playerId);
        
        List<Question> catchUpQuestions = smartReviewService.createCatchUpQuiz(playerId);
        logger.info("Found {} incorrect questions for player ID: '{}'", catchUpQuestions.size(), playerId);

        // Convert the list of Question entities to a list of QuestionDTOs
        List<QuestionDTO> questionDTOs = catchUpQuestions.stream()
                .map(QuestionDTO::new)
                .collect(Collectors.toList());

        if (questionDTOs.isEmpty()) {
            logger.info("No incorrect questions found for player ID: '{}'. Returning empty quiz.", playerId);
            QuizDTO emptyQuiz = new QuizDTO(UUID.randomUUID().toString(), "Smart Review", Collections.emptyList());
            return ResponseEntity.ok(emptyQuiz);
        }

        // Create the final QuizDTO to send to the client
        QuizDTO catchUpQuizDTO = new QuizDTO(
                UUID.randomUUID().toString(), 
                "Quiz de Rattrapage",      
                questionDTOs              
        );
        logger.info("Successfully generated 'Quiz de Rattrapage' with {} questions for player ID: '{}'", questionDTOs.size(), playerId);

        return ResponseEntity.ok(catchUpQuizDTO);
    }
}
