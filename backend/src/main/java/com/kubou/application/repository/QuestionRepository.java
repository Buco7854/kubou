package com.kubou.application.repository;

import com.kubou.domain.entity.Question;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository {
    Question save(Question question);
    Optional<Question> findById(String id);
    List<Question> findAll();
    List<Question> findAllById(List<String> ids);
    List<Question> findByCreatorId(String creatorId); // Added method
}
