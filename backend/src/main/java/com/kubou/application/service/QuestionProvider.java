package com.kubou.application.service;

import com.kubou.domain.entity.Question;
import java.util.List;

public interface QuestionProvider {

    List<Question> fetchQuestions(QuestionProviderRequest request);

    String getSourceIdentifier();

    /**
     * Returns the language code of the questions provided by this source,
     * or null if the source supports multiple languages (language is set via request).
     */
    String getSourceLanguage();
}
