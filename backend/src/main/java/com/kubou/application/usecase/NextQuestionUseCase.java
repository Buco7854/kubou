package com.kubou.application.usecase;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.domain.entity.GameSession;
import com.kubou.domain.entity.GameState;
import com.kubou.domain.entity.Quiz;
import com.kubou.domain.service.IGameModeEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NextQuestionUseCase {

    private final GameSessionRepository gameSessionRepository;
    private final QuizRepository quizRepository;
    private final IGameModeEngine gameModeEngine;

    public NextQuestionUseCase(GameSessionRepository gameSessionRepository, QuizRepository quizRepository, IGameModeEngine gameModeEngine) {
        this.gameSessionRepository = gameSessionRepository;
        this.quizRepository = quizRepository;
        this.gameModeEngine = gameModeEngine;
    }

    @Transactional
    public GameSession execute(String gameId, String userId) {
        GameSession session = gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        if (!session.getHostId().equals(userId)) {
            throw new SecurityException("Only the host can advance the game.");
        }

        Quiz quiz = quizRepository.findById(session.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // The engine determines the next state
        GameState nextState = gameModeEngine.handleNextState(session, quiz.getQuestions().size());
        session.setState(nextState);

        gameSessionRepository.save(session);
        return session;
    }
}
