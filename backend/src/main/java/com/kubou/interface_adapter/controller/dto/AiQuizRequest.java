package com.kubou.interface_adapter.controller.dto;

public class AiQuizRequest {
    private String title;
    private String text;
    private int amount;
    private String language;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}
