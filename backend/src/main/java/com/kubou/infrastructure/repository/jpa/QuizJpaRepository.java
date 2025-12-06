package com.kubou.infrastructure.repository.jpa;

import com.kubou.infrastructure.repository.jpa.model.QuizData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizJpaRepository extends JpaRepository<QuizData, String> {
}
