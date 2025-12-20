package com.kubou.infrastructure.repository.jpa.mapper;

import com.kubou.domain.entity.Question;
import com.kubou.domain.entity.Quiz;
import com.kubou.infrastructure.repository.jpa.model.QuestionData;
import com.kubou.infrastructure.repository.jpa.model.QuizData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuizMapper {

    public QuizData toData(Quiz quiz) {
        if (quiz == null) return null;
        QuizData data = new QuizData();
        data.setId(quiz.getId());
        data.setTitle(quiz.getTitle());
        data.setCreatorId(quiz.getCreatorId());
        
        if (quiz.getQuestions() != null) {
            List<QuestionData> questionDataList = quiz.getQuestions().stream()
                    .map(this::toQuestionData)
                    .collect(Collectors.toList());
            data.setQuestions(questionDataList);
        }
        return data;
    }

    public Quiz toDomain(QuizData data) {
        if (data == null) return null;
        List<Question> questions = data.getQuestions() != null ?
                data.getQuestions().stream().map(this::toQuestionDomain).collect(Collectors.toList()) :
                Collections.emptyList();
        
        return new Quiz(data.getId(), data.getTitle(), questions, data.getCreatorId());
    }

    // Public helper methods for Question mapping
    public QuestionData toQuestionData(Question question) {
        if (question == null) return null;
        QuestionData data = new QuestionData();
        data.setId(question.getId());
        data.setText(question.getText());
        data.setOptions(question.getOptions());
        data.setCorrectAnswerIndex(question.getCorrectAnswerIndex());
        data.setTags(question.getTags());
        data.setDifficultyLevel(question.getDifficultyLevel());
        data.setCreatorId(question.getCreatorId()); // Map creatorId
        return data;
    }

    public Question toQuestionDomain(QuestionData data) {
        if (data == null) return null;
        return new Question(
                data.getId(),
                data.getText(),
                data.getOptions(),
                data.getCorrectAnswerIndex(),
                data.getTags(),
                data.getDifficultyLevel(),
                data.getCreatorId() // Map creatorId
        );
    }
}
