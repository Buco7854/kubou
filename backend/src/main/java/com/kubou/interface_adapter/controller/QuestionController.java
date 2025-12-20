package com.kubou.interface_adapter.controller;

import com.kubou.application.repository.QuestionRepository;
import com.kubou.domain.entity.Question;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/questions")
@SecurityRequirement(name = "bearerAuth")
public class QuestionController {

    private final QuestionRepository questionRepository;

    public QuestionController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @PostMapping
    @Operation(summary = "Create a new question in the question bank")
    public Question createQuestion(@RequestBody Question question, Principal principal) {
        question.setId(UUID.randomUUID().toString());
        question.setCreatorId(principal.getName()); // Set creator ID
        return questionRepository.save(question);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a question by ID")
    public ResponseEntity<Question> getQuestionById(@PathVariable String id) {
        return questionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all questions created by the current user")
    public List<Question> listAllQuestions(Principal principal) {
        return questionRepository.findByCreatorId(principal.getName());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing question")
    public ResponseEntity<Question> updateQuestion(@PathVariable String id, @RequestBody Question updatedQuestion, Principal principal) {
        return questionRepository.findById(id)
                .map(existingQuestion -> {
                    if (!existingQuestion.getCreatorId().equals(principal.getName())) {
                        // Return 403 if not the owner
                        // Since we are inside map, we can't easily return ResponseEntity with status directly without casting or throwing
                        // For simplicity, we'll just return not found or throw exception, but let's try to be clean
                        throw new RuntimeException("Unauthorized"); 
                    }
                    updatedQuestion.setId(id);
                    updatedQuestion.setCreatorId(existingQuestion.getCreatorId()); // Preserve creator
                    return ResponseEntity.ok(questionRepository.save(updatedQuestion));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        if ("Unauthorized".equals(ex.getMessage())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.internalServerError().body(ex.getMessage());
    }
}
