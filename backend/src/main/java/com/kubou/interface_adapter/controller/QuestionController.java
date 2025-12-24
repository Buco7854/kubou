package com.kubou.interface_adapter.controller;

import com.kubou.application.repository.QuestionRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.domain.entity.Question;
import com.kubou.domain.entity.Quiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/questions")
@SecurityRequirement(name = "bearerAuth")
public class QuestionController {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    public QuestionController(QuestionRepository questionRepository, QuizRepository quizRepository) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
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
                        throw new RuntimeException("Unauthorized"); 
                    }
                    updatedQuestion.setId(id);
                    updatedQuestion.setCreatorId(existingQuestion.getCreatorId()); // Preserve creator
                    return ResponseEntity.ok(questionRepository.save(updatedQuestion));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a question by ID")
    public ResponseEntity<Void> deleteQuestion(@PathVariable String id, Principal principal) {
        return questionRepository.findById(id)
                .map(question -> {
                    if (!question.getCreatorId().equals(principal.getName())) {
                        return ResponseEntity.status(403).<Void>build();
                    }

                    // Remove question from all quizzes
                    List<Quiz> allQuizzes = quizRepository.findAll();
                    for (Quiz quiz : allQuizzes) {
                        boolean removed = quiz.getQuestions().removeIf(q -> q.getId().equals(id));
                        if (removed) {
                            quizRepository.save(quiz);
                        }
                    }

                    questionRepository.deleteById(id);
                    
                    return ResponseEntity.noContent().<Void>build();
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
