package com.kubou.application.service;

import com.kubou.domain.entity.Question;

import java.util.List;

/**
 * Service responsible for translating questions from one language to another.
 * Currently a stub that returns questions as-is.
 * Ready to be implemented with a real translation provider (e.g. DeepL, Google Translate, AI).
 */
public interface QuestionTranslationService {

    /**
     * Translates a list of questions from the source language to the target language.
     *
     * @param questions     the questions to translate
     * @param sourceLanguage the language code of the questions (e.g. "en", "fr")
     * @param targetLanguage the desired language code (e.g. "fr", "en")
     * @return the translated questions
     */
    List<Question> translate(List<Question> questions, String sourceLanguage, String targetLanguage);
}
