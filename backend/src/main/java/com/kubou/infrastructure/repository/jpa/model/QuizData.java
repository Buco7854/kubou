package com.kubou.infrastructure.repository.jpa.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class QuizData {
    @Id
    private String id;
    private String title;
    private String creatorId; // Added creatorId

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "quiz_questions",
        joinColumns = @JoinColumn(name = "quiz_id"),
        inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<QuestionData> questions;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public List<QuestionData> getQuestions() { return questions; }
    public void setQuestions(List<QuestionData> questions) { this.questions = questions; }
}
