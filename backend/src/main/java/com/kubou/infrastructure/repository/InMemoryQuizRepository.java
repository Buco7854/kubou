package com.kubou.infrastructure.repository;

import com.kubou.application.repository.QuizRepository;
import com.kubou.domain.entity.Question;
import com.kubou.domain.entity.Quiz;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryQuizRepository implements QuizRepository {

    private final ConcurrentHashMap<String, Quiz> quizzes = new ConcurrentHashMap<>();

    public InMemoryQuizRepository() {
        // Create a dummy quiz for testing
        List<Question> questions = Arrays.asList(
                new Question("1", "What is 2+2?", Arrays.asList("3", "4", "5"), 1, Arrays.asList("Math", "Arithmetic"), 1),
                new Question("2", "What is the capital of France?", Arrays.asList("London", "Berlin", "Paris"), 2, Arrays.asList("Geography"), 1)
        );
        Quiz quiz = new Quiz("dummy-quiz", "Dummy Quiz", questions);
        quizzes.put(quiz.getId(), quiz);
    }

    @Override
    public Optional<Quiz> findById(String id) {
        return Optional.ofNullable(quizzes.get(id));
    }

    @Override
    public Quiz save(Quiz quiz) {
        quizzes.put(quiz.getId(), quiz);
        return quiz;
    }
}
