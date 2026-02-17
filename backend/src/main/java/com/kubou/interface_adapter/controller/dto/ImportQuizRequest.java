package com.kubou.interface_adapter.controller.dto;

import java.util.List;

public class ImportQuizRequest {
    private String title;
    private String source;
    private List<String> tags;
    private int amount;
    private String language;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}
