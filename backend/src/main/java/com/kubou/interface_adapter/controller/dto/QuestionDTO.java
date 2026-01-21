package com.kubou.interface_adapter.controller.dto;

import com.kubou.domain.entity.Question;
import java.util.List;

/**
 * A Data Transfer Object for the Question entity.
 * This is used to send question data to the client without Hibernate proxies.
 */
public class QuestionDTO {
    private String id;
    private String text;
    private List<String> options;
    // Note: We might not want to send the correct answer index to the client immediately
    // depending on the game logic. For a review, it might be fine.
    private int correctAnswerIndex;
    private List<String> tags;
    private int difficultyLevel;

    public QuestionDTO(Question question) {
        this.id = question.getId();
        this.text = question.getText();
        this.options = question.getOptions();
        this.correctAnswerIndex = question.getCorrectAnswerIndex();
        this.tags = question.getTags();
        this.difficultyLevel = question.getDifficultyLevel();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public void setCorrectAnswerIndex(int correctAnswerIndex) { this.correctAnswerIndex = correctAnswerIndex; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public int getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(int difficultyLevel) { this.difficultyLevel = difficultyLevel; }
}
