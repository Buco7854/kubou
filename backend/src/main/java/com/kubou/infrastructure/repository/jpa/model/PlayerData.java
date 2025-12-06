package com.kubou.infrastructure.repository.jpa.model;

import jakarta.persistence.*;

@Entity
@Table(name = "players")
public class PlayerData {
    @Id
    private String id;
    private String nickname;
    private int score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id")
    private GameSessionData gameSession;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public GameSessionData getGameSession() { return gameSession; }
    public void setGameSession(GameSessionData gameSession) { this.gameSession = gameSession; }
}
