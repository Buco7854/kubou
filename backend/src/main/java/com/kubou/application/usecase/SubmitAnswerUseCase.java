package com.kubou.application.usecase;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.application.usecase.dto.SubmitAnswerResult;
import com.kubou.domain.entity.*;
import com.kubou.domain.service.IScoringStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SubmitAnswerUseCase {

    private static final Logger logger = LoggerFactory.getLogger(SubmitAnswerUseCase.class);

    private final GameSessionRepository gameSessionRepository;
    private final QuizRepository quizRepository;
    private final PlayerResponseRepository playerResponseRepository;
    private final IScoringStrategy scoringStrategy;

    public SubmitAnswerUseCase(
            GameSessionRepository gameSessionRepository,
            QuizRepository quizRepository,
            PlayerResponseRepository playerResponseRepository,
            IScoringStrategy scoringStrategy
    ) {
        this.gameSessionRepository = gameSessionRepository;
        this.quizRepository = quizRepository;
        this.playerResponseRepository = playerResponseRepository;
        this.scoringStrategy = scoringStrategy;
    }

    @Transactional
    public SubmitAnswerResult execute(String gameId, UserAnswer userAnswer) {
        // 1. Récupération des données
        GameSession gameSession = gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game session not found"));

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

        // 2. Calcul du Score
        int score = scoringStrategy.calculate(userAnswer, question, gameSession.getGameConfig(), player.getCurrentStreak());
        boolean isCorrect = score > 0;

        // 3. Mise à jour Joueur
        if (isCorrect) {
            player.setScore(player.getScore() + score);
            player.setCurrentStreak(player.getCurrentStreak() + 1);
        } else {
            player.setCurrentStreak(0);
        }
        
        // Mise à jour Équipe
        if (gameSession.isTeamMode()) {
            gameSession.getTeams().stream()
                    .filter(t -> t.getPlayerIds().contains(player.getId()))
                    .findFirst()
                    .ifPresent(team -> team.setScore(team.getScore() + score));
        }

        // 4. Sauvegarde Réponse
        PlayerResponse response = new PlayerResponse(
                UUID.randomUUID().toString(),
                gameId,
                player.getUserId(), // Changed from player.getId() to player.getUserId()
                question.getId(),
                userAnswer.getAnswerIndex(),
                isCorrect,
                userAnswer.getTimeToAnswerMs(),
                score,
                LocalDateTime.now()
        );
        playerResponseRepository.save(response);

        // 5. Vérification Fin de Manche
        List<PlayerResponse> responses = playerResponseRepository.findByGameSessionIdAndQuestionId(gameId, question.getId());
        long answeredCount = responses.stream().map(PlayerResponse::getPlayerId).distinct().count();
        
        boolean currentIncluded = responses.stream().anyMatch(r -> r.getPlayerId().equals(player.getId()));
        if (!currentIncluded) answeredCount++;

        boolean roundFinished = answeredCount >= gameSession.getPlayers().size();
        if (roundFinished) {
            gameSession.setState(GameState.QUESTION_RESULTS);
        }

        // 6. Sauvegarde Session (Points + État)
        gameSessionRepository.save(gameSession);
        
        // On retourne le résultat pour que le Controller gère la suite (Badges + WS)
        return new SubmitAnswerResult(score, roundFinished, isCorrect);
    }
}