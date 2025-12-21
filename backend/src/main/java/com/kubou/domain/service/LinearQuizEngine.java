package com.kubou.domain.service;

import com.kubou.domain.entity.GameSession;
import com.kubou.domain.entity.GameState;
import org.springframework.stereotype.Component;

@Component
public class LinearQuizEngine implements IGameModeEngine {

    @Override
    public GameState handleNextState(GameSession session, int totalQuestions) {
        if (session.getState() == GameState.IN_PROGRESS) {
            // After a question is finished (all answered or timeout), we go to results
            return GameState.QUESTION_RESULTS;
        } else if (session.getState() == GameState.QUESTION_RESULTS) {
            // From results, we go to the next question or finish
            int nextQuestionIndex = session.getCurrentQuestionIndex() + 1;
            if (nextQuestionIndex < totalQuestions) {
                session.setCurrentQuestionIndex(nextQuestionIndex);
                return GameState.IN_PROGRESS;
            } else {
                return GameState.FINISHED;
            }
        }
        // If the game is in Lobby or Finished, it remains in that state.
        return session.getState();
    }
}
