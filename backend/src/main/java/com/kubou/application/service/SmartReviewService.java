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

    public SmartReviewService(PlayerResponseRepository responseRepository,
                              QuestionRepository questionRepository,
                              QuizRepository quizRepository) {
        this.responseRepository = responseRepository;
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
    }

    public Optional<Quiz> generateReviewQuiz(String playerId) {
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();

        List<PlayerResponse> responses =
                responseRepository.findByPlayerIdAndDateRange(playerId, start, end);


        // Count wrong answers per question
        Map<String, Long> wrongCounts = responses.stream()
                .filter(r -> !r.isCorrect())
                .collect(Collectors.groupingBy(PlayerResponse::getQuestionId, Collectors.counting()));

        List<String> topQuestionIds = wrongCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(Map.Entry::getKey)
                .toList();

        if (topQuestionIds.isEmpty()) {
            return Optional.empty();
        }

        List<Question> questionsToReview = questionRepository.findAllById(topQuestionIds);

        if (questionsToReview.isEmpty()) {
            return Optional.empty();
        }

        String title = "Smart Review - " + LocalDateTime.now().toLocalDate();
        Quiz reviewQuiz = new Quiz(UUID.randomUUID().toString(), title, questionsToReview);

        return Optional.of(quizRepository.save(reviewQuiz));
    }
}
