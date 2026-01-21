package com.kubou.application.service;

import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.application.repository.QuestionRepository;
import com.kubou.domain.entity.PlayerResponse;
import com.kubou.domain.entity.Question;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SmartReviewService {

    private final PlayerResponseRepository playerResponseRepository;
    private final QuestionRepository questionRepository;

    public SmartReviewService(PlayerResponseRepository playerResponseRepository, QuestionRepository questionRepository) {
        this.playerResponseRepository = playerResponseRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Identifies questions that a player answered incorrectly.
     * @param playerId The ID of the player.
     * @return A list of question IDs that the player answered incorrectly.
     */
    public List<String> getIncorrectlyAnsweredQuestionIds(String playerId) {
        List<PlayerResponse> playerResponses = playerResponseRepository.findByPlayerId(playerId);

        return playerResponses.stream()
                .filter(response -> !response.isCorrect())
                .map(PlayerResponse::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Creates a "catch-up quiz" with questions the player previously answered incorrectly.
     * @param playerId The ID of the player.
     * @return A list of Question objects for the catch-up quiz.
     */
    public List<Question> createCatchUpQuiz(String playerId) {
        List<String> incorrectQuestionIds = getIncorrectlyAnsweredQuestionIds(playerId);

        // For now, we fetch all incorrect questions.
        // In the future, we could add logic to select a subset of questions,
        // or add new questions from the same categories.
        return questionRepository.findAllById(incorrectQuestionIds);
    }

    // TODO: Add methods for grouping incorrect questions by topic/category
}
