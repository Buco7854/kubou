package com.kubou.infrastructure.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class QuizzApiResponse {
    private int count;
    private List<QuizzItem> quizzes;

    public static class QuizzItem {
        @JsonProperty("_id")
        private String id;
        private String question;
        private String answer;
        private List<String> badAnswers;
        private String category;
        private String difficulty;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        public List<String> getBadAnswers() { return badAnswers; }
        public void setBadAnswers(List<String> badAnswers) { this.badAnswers = badAnswers; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public List<QuizzItem> getQuizzes() { return quizzes; }
    public void setQuizzes(List<QuizzItem> quizzes) { this.quizzes = quizzes; }
}
