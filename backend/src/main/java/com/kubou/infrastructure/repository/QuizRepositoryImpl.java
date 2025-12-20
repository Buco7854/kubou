package com.kubou.infrastructure.repository;

import com.kubou.application.repository.QuizRepository;
import com.kubou.domain.entity.Quiz;
import com.kubou.infrastructure.repository.jpa.QuizJpaRepository;
import com.kubou.infrastructure.repository.jpa.mapper.QuizMapper;
import com.kubou.infrastructure.repository.jpa.model.QuizData;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> findAll() {
        return quizJpaRepository.findAll().stream()
                .map(quizMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> findByCreatorId(String creatorId) {
        return quizJpaRepository.findByCreatorId(creatorId).stream()
                .map(quizMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        quizJpaRepository.deleteById(id);
    }
}
