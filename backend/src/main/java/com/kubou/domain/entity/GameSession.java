package com.kubou.domain.entity;

import java.util.ArrayList;
import java.util.List;

public class GameSession {
    private String id;
    private String pin;
    private String quizId;
    private String hostId; // Added host identifier
    private List<Player> players;
    private int currentQuestionIndex;
    private GameState state;

    public GameSession(String id, String pin, String quizId, String hostId) {
        this.id = id;
        this.pin = pin;
        this.quizId = quizId;
        this.hostId = hostId;
        this.players = new ArrayList<>();
        this.currentQuestionIndex = -1; // -1 indicates game has not started
        this.state = GameState.LOBBY;
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

    public void addPlayer(Player player) {
        this.players.add(player);
    }
}
