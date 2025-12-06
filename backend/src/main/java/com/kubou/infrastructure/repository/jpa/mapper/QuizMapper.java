package com.kubou.infrastructure.repository.jpa.mapper;

import com.kubou.domain.entity.Question;
import com.kubou.domain.entity.Quiz;
import com.kubou.infrastructure.repository.jpa.model.QuestionData;
import com.kubou.infrastructure.repository.jpa.model.QuizData;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class QuizMapper {

    public QuizData toData(Quiz quiz) {
        if (quiz == null) {
            return null;
        }
        QuizData quizData = new QuizData();
        quizData.setId(quiz.getId());
        quizData.setTitle(quiz.getTitle());
        if (quiz.getQuestions() != null) {
            // The ManyToMany relationship is managed by JPA, just set the list of questions
            quizData.setQuestions(quiz.getQuestions().stream().map(this::toData).collect(Collectors.toList()));
        }
        return quizData;
    }

    public QuestionData toData(Question question) {
        if (question == null) {
            return null;
        }
        QuestionData questionData = new QuestionData();
        questionData.setId(question.getId());
        questionData.setText(question.getText());
        questionData.setOptions(question.getOptions());
        questionData.setCorrectAnswerIndex(question.getCorrectAnswerIndex());
        questionData.setTags(question.getTags());
        questionData.setDifficultyLevel(question.getDifficultyLevel());
        return questionData;
    }

    public Quiz toDomain(QuizData quizData) {
        if (quizData == null) {
            return null;
        }
        return new Quiz(
                quizData.getId(),
                quizData.getTitle(),
                quizData.getQuestions().stream().map(this::toDomain).collect(Collectors.toList())
        );
    }

    public Question toDomain(QuestionData questionData) {
        if (questionData == null) {
            return null;
        }
        return new Question(
                questionData.getId(),
                questionData.getText(),
                questionData.getOptions(),
                questionData.getCorrectAnswerIndex(),
                questionData.getTags(),
                questionData.getDifficultyLevel()
        );
    }
}
