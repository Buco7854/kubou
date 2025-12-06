package com.kubou.interface_adapter.controller;

import com.kubou.application.repository.QuestionRepository;
import com.kubou.domain.entity.Question;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public Question createQuestion(@RequestBody Question question) {
        question.setId(UUID.randomUUID().toString());
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
    @Operation(summary = "List all questions in the bank")
    public List<Question> listAllQuestions() {
        return questionRepository.findAll();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing question")
    public ResponseEntity<Question> updateQuestion(@PathVariable String id, @RequestBody Question updatedQuestion) {
        return questionRepository.findById(id)
                .map(existingQuestion -> {
                    updatedQuestion.setId(id); // Ensure the ID is not changed
                    return ResponseEntity.ok(questionRepository.save(updatedQuestion));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
