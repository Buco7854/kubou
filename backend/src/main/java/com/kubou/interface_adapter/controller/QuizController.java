package com.kubou.interface_adapter.controller;

import com.kubou.application.repository.QuestionRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.domain.entity.Question;
import com.kubou.domain.entity.Quiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quizzes")
@SecurityRequirement(name = "bearerAuth")
public class QuizController {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    public QuizController(QuizRepository quizRepository, QuestionRepository questionRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    @PostMapping
    @Operation(summary = "Create a new, empty quiz")
    public Quiz createQuiz(@RequestBody QuizCreationRequest request) {
        Quiz newQuiz = new Quiz(UUID.randomUUID().toString(), request.getTitle(), Collections.emptyList());
        quizRepository.save(newQuiz);
        return newQuiz;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get quiz details by ID, including its questions")
    public Quiz getQuizById(@PathVariable String id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with ID: " + id));
    }

    @PostMapping("/{quizId}/questions")
    @Operation(summary = "Add questions to a quiz by their IDs")
    public ResponseEntity<Quiz> addQuestionsToQuiz(@PathVariable String quizId, @RequestBody QuestionAssignmentRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with ID: " + quizId));
        
        List<Question> questionsToAdd = questionRepository.findAllById(request.getQuestionIds());
        // You might want to add a check here to ensure all requested IDs were found
        
        quiz.getQuestions().addAll(questionsToAdd);
        quizRepository.save(quiz);
        
        return ResponseEntity.ok(quiz);
    }

    // DTO for Quiz Creation
    public static class QuizCreationRequest {
        private String title;
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }

    // DTO for assigning questions
    public static class QuestionAssignmentRequest {
        private List<String> questionIds;
        public List<String> getQuestionIds() { return questionIds; }
        public void setQuestionIds(List<String> questionIds) { this.questionIds = questionIds; }
    }
}
