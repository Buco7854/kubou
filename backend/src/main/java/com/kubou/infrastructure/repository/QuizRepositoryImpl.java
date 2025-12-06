package com.kubou.infrastructure.repository;

import com.kubou.application.repository.QuizRepository;
import com.kubou.domain.entity.Quiz;
import com.kubou.infrastructure.repository.jpa.QuizJpaRepository;
import com.kubou.infrastructure.repository.jpa.mapper.QuizMapper;
import com.kubou.infrastructure.repository.jpa.model.QuizData;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component("jpaQuizRepository")
public class QuizRepositoryImpl implements QuizRepository {

    private final QuizJpaRepository quizJpaRepository;
    private final QuizMapper quizMapper;

    public QuizRepositoryImpl(QuizJpaRepository quizJpaRepository, QuizMapper quizMapper) {
        this.quizJpaRepository = quizJpaRepository;
        this.quizMapper = quizMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Quiz> findById(String id) {
        return quizJpaRepository.findById(id).map(quizMapper::toDomain);
    }

    @Override
    @Transactional
    public Quiz save(Quiz quiz) {
        QuizData quizData = quizMapper.toData(quiz);
        QuizData savedData = quizJpaRepository.save(quizData);
        return quizMapper.toDomain(savedData);
    }
}
