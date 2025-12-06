package com.kubou.application.repository;

import com.kubou.domain.entity.Quiz;
import java.util.Optional;

public interface QuizRepository {
    Optional<Quiz> findById(String id);
    Quiz save(Quiz quiz);
}
