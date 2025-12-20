package com.kubou.infrastructure.repository.jpa.model;

import com.kubou.domain.entity.GameState;
import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

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
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "game_session_id")
    private List<TeamData> teams;

    private int currentQuestionIndex;

    @Enumerated(EnumType.STRING)
    private GameState state;

    // Scoring Settings
    private int baseScore;
    private double timeWeight;
    private double streakMultiplier;
    
    // Team Mode
    private boolean isTeamMode;

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
    public List<TeamData> getTeams() { return teams; }
    public void setTeams(List<TeamData> teams) { this.teams = teams; }
    public int getCurrentQuestionIndex() { return currentQuestionIndex; }
    public void setCurrentQuestionIndex(int currentQuestionIndex) { this.currentQuestionIndex = currentQuestionIndex; }
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }

    public int getBaseScore() { return baseScore; }
    public void setBaseScore(int baseScore) { this.baseScore = baseScore; }
    public double getTimeWeight() { return timeWeight; }
    public void setTimeWeight(double timeWeight) { this.timeWeight = timeWeight; }
    public double getStreakMultiplier() { return streakMultiplier; }
    public void setStreakMultiplier(double streakMultiplier) { this.streakMultiplier = streakMultiplier; }
    
    public boolean isTeamMode() { return isTeamMode; }
    public void setTeamMode(boolean teamMode) { isTeamMode = teamMode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameSessionData that = (GameSessionData) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
