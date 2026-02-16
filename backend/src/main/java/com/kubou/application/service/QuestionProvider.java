package com.kubou.application.service;

import com.kubou.domain.entity.Question;
import java.util.List;

public interface QuestionProvider {

    List<Question> fetchQuestions(QuestionProviderRequest request);

    String getSourceIdentifier();
}
