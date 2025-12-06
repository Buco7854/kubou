package com.kubou.domain.entity;

public class Player {
    private String id;
    private String nickname;
    private int score;

    public Player(String id, String nickname) {
        this.id = id;
        this.nickname = nickname;
        this.score = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
