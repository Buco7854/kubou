package com.kubou.infrastructure.config;

import com.kubou.application.repository.GameSessionRepository;
import com.kubou.application.repository.PlayerAchievementRepository;
import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.application.repository.QuestionRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.domain.service.CustomScoringStrategy;
import com.kubou.domain.service.IScoringStrategy;
import com.kubou.infrastructure.repository.GameSessionRepositoryImpl;
import com.kubou.infrastructure.repository.PlayerAchievementRepositoryImpl;
import com.kubou.infrastructure.repository.PlayerResponseRepositoryImpl;
import com.kubou.infrastructure.repository.QuestionRepositoryImpl;
import com.kubou.infrastructure.repository.QuizRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class BeanConfig {

    @Bean
    public IScoringStrategy scoringStrategy() {
        return new CustomScoringStrategy();
    }

    @Bean
    public RestClient restClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
