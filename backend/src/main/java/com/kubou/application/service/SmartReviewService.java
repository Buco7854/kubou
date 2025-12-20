package com.kubou.application.service;

import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.application.repository.QuestionRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.domain.entity.PlayerResponse;
import com.kubou.domain.entity.Question;
import com.kubou.domain.entity.Quiz;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SmartReviewService {

    private final PlayerResponseRepository responseRepository;
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    public SmartReviewService(PlayerResponseRepository responseRepository, QuestionRepository questionRepository, QuizRepository quizRepository) {
        this.responseRepository = responseRepository;
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
    }

    public Quiz generateReviewQuiz(String playerId) {
        // Look at last 30 days
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();
        
        List<PlayerResponse> responses = responseRepository.findByPlayerIdAndDateRange(playerId, start, end);
        
        // Filter for incorrect answers
        List<String> incorrectQuestionIds = responses.stream()
                .filter(r -> !r.isCorrect())
                .map(PlayerResponse::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        
        if (incorrectQuestionIds.isEmpty()) {
            throw new RuntimeException("No incorrect answers found to review!");
        }
        
        // Fetch full question objects
        List<Question> questionsToReview = questionRepository.findAllById(incorrectQuestionIds);
        
        // Create a new Quiz
        String title = "Smart Review - " + LocalDateTime.now().toLocalDate();
        Quiz reviewQuiz = new Quiz(UUID.randomUUID().toString(), title, questionsToReview);
        
        return quizRepository.save(reviewQuiz);
    }
}
