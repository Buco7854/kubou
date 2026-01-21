package com.kubou.application.usecase;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.application.service.AchievementService;
import com.kubou.domain.entity.*;
import com.kubou.domain.service.IScoringStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SubmitAnswerUseCase {

    private final GameSessionRepository gameSessionRepository;
    private final QuizRepository quizRepository;
    private final PlayerResponseRepository playerResponseRepository;
    private final IScoringStrategy scoringStrategy;
    private final AchievementService achievementService;

    public SubmitAnswerUseCase(
            GameSessionRepository gameSessionRepository,
            QuizRepository quizRepository,
            PlayerResponseRepository playerResponseRepository,
            IScoringStrategy scoringStrategy,
            AchievementService achievementService
    ) {
        this.gameSessionRepository = gameSessionRepository;
        this.quizRepository = quizRepository;
        this.playerResponseRepository = playerResponseRepository;
        this.scoringStrategy = scoringStrategy;
        this.achievementService = achievementService;
    }

    @Transactional
    public int execute(String gameId, UserAnswer userAnswer) {
        // Fetch fresh session to get latest player count
        GameSession gameSession = gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game session not found"));

        if (gameSession.getState() != GameState.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress");
        }

        Quiz quiz = quizRepository.findById(gameSession.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        Question question = quiz.getQuestions().stream()
                .filter(q -> q.getId().equals(userAnswer.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Player player = gameSession.getPlayers().stream()
                .filter(p -> p.getId().equals(userAnswer.getPlayerId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Player not found"));

        // Calculate score using current streak
        int score = scoringStrategy.calculate(userAnswer, question, gameSession.getGameConfig(), player.getCurrentStreak());
        boolean isCorrect = score > 0; // Simplified check

        // Update player stats
        if (isCorrect) {
            player.setScore(player.getScore() + score);
            player.setCurrentStreak(player.getCurrentStreak() + 1);
        } else {
            player.setCurrentStreak(0);
        }
        
        // Update Team Score if in Team Mode
        if (gameSession.isTeamMode()) {
            gameSession.getTeams().stream()
                    .filter(t -> t.getPlayerIds().contains(player.getId()))
                    .findFirst()
                    .ifPresent(team -> team.setScore(team.getScore() + score));
        }
        
        // Check achievements
        achievementService.checkAndUnlockAchievements(player, userAnswer, isCorrect);
        
        // Save raw response for analytics
        PlayerResponse response = new PlayerResponse(
                UUID.randomUUID().toString(),
                gameId,
                player.getId(),
                question.getId(),
                userAnswer.getAnswerIndex(),
                isCorrect,
                userAnswer.getTimeToAnswerMs(),
                score,
                LocalDateTime.now()
        );
        playerResponseRepository.save(response);
        
        // Check if all players have answered
        List<PlayerResponse> responses = playerResponseRepository.findByGameSessionIdAndQuestionId(gameId, question.getId());
        
        long answeredCount = responses.stream().map(PlayerResponse::getPlayerId).distinct().count();
        
        // IMPORTANT: We must compare against the CURRENT number of players in the session.
        // Since we fetched the session at the start of the method, gameSession.getPlayers().size() is up to date.
        if (answeredCount >= gameSession.getPlayers().size()) {
            // All players answered, move to QUESTION_RESULTS state
            gameSession.setState(GameState.QUESTION_RESULTS);
        }

        gameSessionRepository.save(gameSession);
        
        return score;
    }
}
