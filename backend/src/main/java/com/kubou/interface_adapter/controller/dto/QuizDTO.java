package com.kubou.interface_adapter.controller.dto;

import java.util.List;

/**
 * A Data Transfer Object for the Quiz entity.
 */
public class QuizDTO {
    private String id;
    private String title;
    private List<QuestionDTO> questions;

    public QuizDTO(String id, String title, List<QuestionDTO> questions) {
        this.id = id;
        this.title = title;
        this.questions = questions;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<QuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDTO> questions) { this.questions = questions; }
}
