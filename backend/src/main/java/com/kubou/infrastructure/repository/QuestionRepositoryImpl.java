package com.kubou.infrastructure.repository;

import com.kubou.application.repository.QuestionRepository;
import com.kubou.domain.entity.Question;
import com.kubou.infrastructure.repository.jpa.QuestionJpaRepository;
import com.kubou.infrastructure.repository.jpa.mapper.QuizMapper;
import com.kubou.infrastructure.repository.jpa.model.QuestionData;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class QuestionRepositoryImpl implements QuestionRepository {

    private final QuestionJpaRepository questionJpaRepository;
    private final QuizMapper quizMapper;

    public QuestionRepositoryImpl(QuestionJpaRepository questionJpaRepository, QuizMapper quizMapper) {
        this.questionJpaRepository = questionJpaRepository;
        this.quizMapper = quizMapper;
    }

    @Override
    @Transactional
    public Question save(Question question) {
        QuestionData questionData = quizMapper.toQuestionData(question);
        QuestionData savedData = questionJpaRepository.save(questionData);
        return quizMapper.toQuestionDomain(savedData);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Question> findById(String id) {
        return questionJpaRepository.findById(id).map(quizMapper::toQuestionDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findAll() {
        return questionJpaRepository.findAll().stream()
                .map(quizMapper::toQuestionDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findAllById(List<String> ids) {
        return questionJpaRepository.findAllById(ids).stream()
                .map(quizMapper::toQuestionDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findByCreatorId(String creatorId) {
        return questionJpaRepository.findByCreatorId(creatorId).stream()
                .map(quizMapper::toQuestionDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        questionJpaRepository.deleteById(id);
    }
}
