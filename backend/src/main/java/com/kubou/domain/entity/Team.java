package com.kubou.domain.entity;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String id;
    private String name;
    private int score;
    private List<String> playerIds; // References to players in this team

    public Team(String id, String name) {
        this.id = id;
        this.name = name;
        this.score = 0;
        this.playerIds = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public List<String> getPlayerIds() { return playerIds; }
    public void setPlayerIds(List<String> playerIds) { this.playerIds = playerIds; }
    
    public void addPlayer(String playerId) {
        this.playerIds.add(playerId);
    }
}
