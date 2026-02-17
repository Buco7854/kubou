package com.kubou.application.usecase;

import com.kubou.application.repository.QuestionRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.application.service.QuestionProvider;
import com.kubou.application.service.QuestionProviderFactory;
import com.kubou.application.service.QuestionProviderRequest;
import com.kubou.application.service.QuestionTranslationService;
import com.kubou.domain.entity.Question;
import com.kubou.domain.entity.Quiz;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GenerateQuizUseCase {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final QuestionProviderFactory providerFactory;
    private final QuestionTranslationService translationService;

    public GenerateQuizUseCase(
            QuestionRepository questionRepository,
            QuizRepository quizRepository,
            QuestionProviderFactory providerFactory,
            QuestionTranslationService translationService) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.providerFactory = providerFactory;
        this.translationService = translationService;
    }

    @Transactional
    public Quiz execute(String title, String creatorId, String source,
                        QuestionProviderRequest providerRequest) {
        QuestionProvider provider = providerFactory.getProvider(source);

        List<Question> questions = provider.fetchQuestions(providerRequest);

        // Translate questions if the source language differs from the requested language
        String sourceLanguage = provider.getSourceLanguage();
        String targetLanguage = providerRequest.getLanguage();
        if (targetLanguage != null && sourceLanguage != null
                && !sourceLanguage.equalsIgnoreCase(targetLanguage)) {
            questions = translationService.translate(questions, sourceLanguage, targetLanguage);
        }

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
