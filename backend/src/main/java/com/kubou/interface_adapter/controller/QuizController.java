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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public Quiz createQuiz(@RequestBody QuizCreationRequest request, Principal principal) {
        // Use principal name (user ID) as creator ID
        String creatorId = principal.getName();
        Quiz newQuiz = new Quiz(UUID.randomUUID().toString(), request.getTitle(), Collections.emptyList(), creatorId);
        quizRepository.save(newQuiz);
        return newQuiz;
    }

    @GetMapping
    @Operation(summary = "List all quizzes created by the current user")
    public List<Quiz> listQuizzes(Principal principal) {
        // Filter by creator ID
        String userId = principal.getName();
        return quizRepository.findByCreatorId(userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get quiz details by ID, including its questions")
    public ResponseEntity<Quiz> getQuizById(@PathVariable String id, Principal principal) {
        return quizRepository.findById(id)
                .map(quiz -> {
                    // Optional: Check if user is owner or if quiz is public
                    // For now, allow access if they have the ID (e.g. for playing)
                    // But for editing, frontend should check ownership or we enforce here
                    return ResponseEntity.ok(quiz);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a quiz by ID")
    public ResponseEntity<Void> deleteQuiz(@PathVariable String id, Principal principal) {
        return quizRepository.findById(id)
                .map(quiz -> {
                    if (!quiz.getCreatorId().equals(principal.getName())) {
                        return ResponseEntity.status(403).<Void>build();
                    }
                    quizRepository.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{quizId}/questions")
    @Operation(summary = "Add questions to a quiz by their IDs")
    public ResponseEntity<Quiz> addQuestionsToQuiz(@PathVariable String quizId, @RequestBody QuestionAssignmentRequest request, Principal principal) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with ID: " + quizId));
        
        if (!quiz.getCreatorId().equals(principal.getName())) {
            return ResponseEntity.status(403).build();
        }
        
        List<Question> questionsToAdd = questionRepository.findAllById(request.getQuestionIds());
        
        quiz.getQuestions().addAll(questionsToAdd);
        quizRepository.save(quiz);
        
        return ResponseEntity.ok(quiz);
    }

    @DeleteMapping("/{quizId}/questions/{questionId}")
    @Operation(summary = "Remove a question from a quiz")
    public ResponseEntity<Quiz> removeQuestionFromQuiz(@PathVariable String quizId, @PathVariable String questionId, Principal principal) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with ID: " + quizId));

        if (!quiz.getCreatorId().equals(principal.getName())) {
            return ResponseEntity.status(403).build();
        }

        List<Question> updatedQuestions = quiz.getQuestions().stream()
                .filter(q -> !q.getId().equals(questionId))
                .collect(Collectors.toList());

        quiz.setQuestions(updatedQuestions);
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
