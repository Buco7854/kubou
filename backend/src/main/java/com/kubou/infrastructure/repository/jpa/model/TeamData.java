package com.kubou.infrastructure.repository.jpa.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "teams")
public class TeamData {
    @Id
    private String id;
    private String name;
    private int score;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "team_players", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "player_id")
    private List<String> playerIds;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public List<String> getPlayerIds() { return playerIds; }
    public void setPlayerIds(List<String> playerIds) { this.playerIds = playerIds; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamData teamData = (TeamData) o;
        return Objects.equals(id, teamData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
