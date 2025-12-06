package com.kubou.infrastructure.config;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.application.usecase.JoinGameUseCase;
import com.kubou.application.usecase.SubmitAnswerUseCase;
import com.kubou.domain.service.IGameModeEngine;
import com.kubou.domain.service.IScoringStrategy;
import com.kubou.domain.service.LinearQuizEngine;
import com.kubou.domain.service.SimpleScoringStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public IScoringStrategy scoringStrategy() {
        return new SimpleScoringStrategy();
    }

    @Bean
    public IGameModeEngine gameModeEngine() {
        return new LinearQuizEngine();
    }

    @Bean
    public JoinGameUseCase joinGameUseCase(GameSessionRepository gameSessionRepository) {
        return new JoinGameUseCase(gameSessionRepository);
    }

    @Bean
    public SubmitAnswerUseCase submitAnswerUseCase(
            GameSessionRepository gameSessionRepository,
            QuizRepository quizRepository,
            IScoringStrategy scoringStrategy
    ) {
        return new SubmitAnswerUseCase(gameSessionRepository, quizRepository, scoringStrategy);
    }
}
