package com.kubou.domain.entity;

public class Player {
    private String id;
    private String userId; // Can be null for anonymous users if we don't track them across sessions
    private String nickname;
    private int score;
    private int currentStreak;

    public Player(String id, String nickname) {
        this.id = id;
        this.nickname = nickname;
        this.score = 0;
        this.currentStreak = 0;
    }

    public Player(String id, String userId, String nickname) {
        this.id = id;
        this.userId = userId;
        this.nickname = nickname;
        this.score = 0;
        this.currentStreak = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
}
