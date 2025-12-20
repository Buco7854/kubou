package com.kubou.domain.entity;

import java.util.ArrayList;
import java.util.List;

public class GameSession {
    private String id;
    private String pin;
    private String quizId;
    private String hostId;
    private List<Player> players;
    private int currentQuestionIndex;
    private GameState state;
    private GameConfig gameConfig;
    
    // Team Mode Support
    private boolean isTeamMode;
    private List<Team> teams;

    public GameSession(String id, String pin, String quizId, String hostId) {
        this.id = id;
        this.pin = pin;
        this.quizId = quizId;
        this.hostId = hostId;
        this.players = new ArrayList<>();
        this.currentQuestionIndex = -1;
        this.state = GameState.LOBBY;
        this.gameConfig = new GameConfig();
        this.isTeamMode = false;
        this.teams = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }
    public String getHostId() { return hostId; }
    public void setHostId(String hostId) { this.hostId = hostId; }
    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }
    public int getCurrentQuestionIndex() { return currentQuestionIndex; }
    public void setCurrentQuestionIndex(int currentQuestionIndex) { this.currentQuestionIndex = currentQuestionIndex; }
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public GameConfig getGameConfig() { return gameConfig; }
    public void setGameConfig(GameConfig gameConfig) { this.gameConfig = gameConfig; }
    
    public boolean isTeamMode() { return isTeamMode; }
    public void setTeamMode(boolean teamMode) { isTeamMode = teamMode; }
    public List<Team> getTeams() { return teams; }
    public void setTeams(List<Team> teams) { this.teams = teams; }

    public void addPlayer(Player player) {
        this.players.add(player);
    }
}
