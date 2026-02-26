package com.kubou.infrastructure.provider.dto;

import java.util.List;

public class QuizApiResponse {

    private boolean success;
    private List<BrowseQuestion> data;
    private Meta meta;

    public static class BrowseQuestion {
        private int id;
        private String text;
        private String type;
        private String difficulty;
        private String explanation;
        private String category;
        private List<String> tags;
        private String quizId;
        private String quizTitle;
        private List<Answer> answers;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
        public String getQuizId() { return quizId; }
        public void setQuizId(String quizId) { this.quizId = quizId; }
        public String getQuizTitle() { return quizTitle; }
        public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }
        public List<Answer> getAnswers() { return answers; }
        public void setAnswers(List<Answer> answers) { this.answers = answers; }
    }

    public static class Answer {
        private int id;
        private String text;
        private boolean isCorrect;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public boolean isCorrect() { return isCorrect; }
        public void setCorrect(boolean correct) { isCorrect = correct; }
    }

    public static class Meta {
        private int total;
        private int limit;
        private int offset;

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }
        public int getOffset() { return offset; }
        public void setOffset(int offset) { this.offset = offset; }
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public List<BrowseQuestion> getData() { return data; }
    public void setData(List<BrowseQuestion> data) { this.data = data; }
    public Meta getMeta() { return meta; }
    public void setMeta(Meta meta) { this.meta = meta; }
}
