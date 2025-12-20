package com.kubou.domain.entity;

import java.util.List;

public class Quiz {
    private String id;
    private String title;
    private List<Question> questions;
    private String creatorId; // Added creatorId to track ownership

    public Quiz(String id, String title, List<Question> questions) {
        this.id = id;
        this.title = title;
        this.questions = questions;
    }

    public Quiz(String id, String title, List<Question> questions, String creatorId) {
        this.id = id;
        this.title = title;
        this.questions = questions;
        this.creatorId = creatorId;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
}
