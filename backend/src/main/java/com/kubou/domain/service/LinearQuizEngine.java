package com.kubou.domain.service;

import com.kubou.domain.entity.GameSession;
import com.kubou.domain.entity.GameState;
import org.springframework.stereotype.Component;

@Component
public class LinearQuizEngine implements IGameModeEngine {

    @Override
    public GameState handleNextState(GameSession session, int totalQuestions) {
        if (session.getState() == GameState.IN_PROGRESS) {
            // If we are currently IN_PROGRESS, it means the host clicked "Next" (or timeout happened)
            // while players were answering.
            // The logical next step is to show the results for THIS question.
            return GameState.QUESTION_RESULTS;
        } else if (session.getState() == GameState.QUESTION_RESULTS) {
            // If we are currently showing results, the next step is either the NEXT question
            // or FINISH if there are no more questions.
            int nextQuestionIndex = session.getCurrentQuestionIndex() + 1;
            if (nextQuestionIndex < totalQuestions) {
                session.setCurrentQuestionIndex(nextQuestionIndex);
                return GameState.IN_PROGRESS;
            } else {
                return GameState.FINISHED;
            }
        } else if (session.getState() == GameState.LOBBY) {
            // From Lobby, we start the first question (index 0)
            session.setCurrentQuestionIndex(0);
            return GameState.IN_PROGRESS;
        }

        return session.getState();
    }
}
