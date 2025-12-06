package com.kubou.infrastructure.repository.jpa.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "questions")
public class QuestionData {
    @Id
    private String id;
    private String text;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="question_options", joinColumns=@JoinColumn(name="question_id"))
    @Column(name="option")
    private List<String> options;
    private int correctAnswerIndex;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="question_tags", joinColumns=@JoinColumn(name="question_id"))
    @Column(name="tag")
    private List<String> tags;
    private int difficultyLevel;

    // Note: The explicit relationship to QuizData is removed.
    // The ManyToMany relationship is now owned by QuizData.

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
