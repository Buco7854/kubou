package com.kubou.application.usecase;

import com.kubou.application.repository.QuestionRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.application.service.QuestionProvider;
import com.kubou.application.service.QuestionProviderRequest;
import com.kubou.domain.entity.Question;
import com.kubou.domain.entity.Quiz;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GenerateQuizUseCase {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final Map<String, QuestionProvider> providerMap;

    public GenerateQuizUseCase(
            QuestionRepository questionRepository,
            QuizRepository quizRepository,
            List<QuestionProvider> providers) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(
                        QuestionProvider::getSourceIdentifier,
                        Function.identity()));
    }

    @Transactional
    public Quiz execute(String title, String creatorId, String source,
                        QuestionProviderRequest providerRequest) {
        QuestionProvider provider = providerMap.get(source);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown question source: " + source);
        }

        List<Question> questions = provider.fetchQuestions(providerRequest);

        for (Question question : questions) {
            question.setId(UUID.randomUUID().toString());
            question.setCreatorId(creatorId);
        }

        List<Question> savedQuestions = questionRepository.saveAll(questions);

        Quiz quiz = new Quiz(
                UUID.randomUUID().toString(),
                title,
                savedQuestions,
                creatorId);
        return quizRepository.save(quiz);
    }
}
