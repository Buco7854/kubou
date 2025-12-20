package com.kubou.infrastructure.repository.jpa;

import com.kubou.infrastructure.repository.jpa.model.QuestionData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionJpaRepository extends JpaRepository<QuestionData, String> {
    List<QuestionData> findByCreatorId(String creatorId);
}
