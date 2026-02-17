package com.kubou.application.service;

import java.util.List;

public class QuestionProviderRequest {
    private List<String> tags;
    private int amount;
    private String language;

    public QuestionProviderRequest() {
    }

    public QuestionProviderRequest(List<String> tags, int amount) {
        this.tags = tags;
        this.amount = amount;
    }

    public QuestionProviderRequest(List<String> tags, int amount, String language) {
        this.tags = tags;
        this.amount = amount;
        this.language = language;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
