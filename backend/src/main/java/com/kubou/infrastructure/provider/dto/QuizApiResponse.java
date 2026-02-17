package com.kubou.infrastructure.provider.dto;

import java.util.List;
import java.util.Map;

public class QuizApiResponse {
    private int id;
    private String question;
    private String description;
    private Map<String, String> answers;
    private String multiple_correct_answers;
    private Map<String, String> correct_answers;
    private String explanation;
    private String tip;
    private List<Tag> tags;
    private String category;
    private String difficulty;

    public static class Tag {
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Map<String, String> getAnswers() { return answers; }
    public void setAnswers(Map<String, String> answers) { this.answers = answers; }
    public String getMultiple_correct_answers() { return multiple_correct_answers; }
    public void setMultiple_correct_answers(String multiple_correct_answers) { this.multiple_correct_answers = multiple_correct_answers; }
    public Map<String, String> getCorrect_answers() { return correct_answers; }
    public void setCorrect_answers(Map<String, String> correct_answers) { this.correct_answers = correct_answers; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getTip() { return tip; }
    public void setTip(String tip) { this.tip = tip; }
    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}
