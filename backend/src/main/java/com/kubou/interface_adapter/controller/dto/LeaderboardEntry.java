package com.kubou.interface_adapter.controller.dto;

public class LeaderboardEntry {
    private String userId;
    private String nickname;
    private int score;
    private int currentStreak;
    private String teamName;

    public LeaderboardEntry(String userId, String nickname, int score, int currentStreak, String teamName) {
        this.userId = userId;
        this.nickname = nickname;
        this.score = score;
        this.currentStreak = currentStreak;
        this.teamName = teamName;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getNickname() { return nickname; }
    public int getScore() { return score; }
    public int getCurrentStreak() { return currentStreak; }
    public String getTeamName() { return teamName; }
}
