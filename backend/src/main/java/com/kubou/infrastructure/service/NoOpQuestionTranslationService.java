package com.kubou.infrastructure.service;

import com.kubou.application.service.QuestionTranslationService;
import com.kubou.domain.entity.Question;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * No-op implementation of QuestionTranslationService.
 * Returns questions unchanged. Placeholder for a real translation integration.
 */
@Service
public class NoOpQuestionTranslationService implements QuestionTranslationService {

    @Override
    public List<Question> translate(List<Question> questions, String sourceLanguage, String targetLanguage) {
        // TODO: Integrate a real translation service (DeepL, Google Translate, AI, etc.)
        return questions;
    }
}
