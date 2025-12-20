package com.kubou.application.repository;

import com.kubou.domain.entity.Quiz;
import java.util.List;
import java.util.Optional;

public interface QuizRepository {
    Optional<Quiz> findById(String id);
    Quiz save(Quiz quiz);
    List<Quiz> findAll();
    List<Quiz> findByCreatorId(String creatorId); // Added method
    void deleteById(String id);
}
