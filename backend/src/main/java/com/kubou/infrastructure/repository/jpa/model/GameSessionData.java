package com.kubou.infrastructure.repository.jpa.model;

import com.kubou.domain.entity.GameState;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "game_sessions")
public class GameSessionData {
    @Id
    private String id;
    @Column(unique = true)
    private String pin;
    private String quizId;
    private String hostId;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PlayerData> players;

    private int currentQuestionIndex;

    @Enumerated(EnumType.STRING)
    private GameState state;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }
    public String getHostId() { return hostId; }
    public void setHostId(String hostId) { this.hostId = hostId; }
    public List<PlayerData> getPlayers() { return players; }
    public void setPlayers(List<PlayerData> players) { this.players = players; }
    public int getCurrentQuestionIndex() { return currentQuestionIndex; }
    public void setCurrentQuestionIndex(int currentQuestionIndex) { this.currentQuestionIndex = currentQuestionIndex; }
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
}
