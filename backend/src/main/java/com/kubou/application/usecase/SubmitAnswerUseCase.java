package com.kubou.application.usecase;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.domain.entity.*;
import com.kubou.domain.service.IScoringStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmitAnswerUseCase {

    private final GameSessionRepository gameSessionRepository;
    private final QuizRepository quizRepository;
    private final IScoringStrategy scoringStrategy;

    public SubmitAnswerUseCase(
            GameSessionRepository gameSessionRepository,
            QuizRepository quizRepository,
            IScoringStrategy scoringStrategy
    ) {
        this.gameSessionRepository = gameSessionRepository;
        this.quizRepository = quizRepository;
        this.scoringStrategy = scoringStrategy;
    }

    @Transactional
    public int execute(String gameId, UserAnswer userAnswer) {
        GameSession gameSession = gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game session not found"));

        Quiz quiz = quizRepository.findById(gameSession.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        Question question = quiz.getQuestions().stream()
                .filter(q -> q.getId().equals(userAnswer.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Question not found"));

        int score = scoringStrategy.calculate(userAnswer, question, new GameConfig());

        Player player = gameSession.getPlayers().stream()
                .filter(p -> p.getId().equals(userAnswer.getPlayerId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Player not found"));

        player.setScore(player.getScore() + score);
        gameSessionRepository.save(gameSession);
        
        return score;
    }
}
