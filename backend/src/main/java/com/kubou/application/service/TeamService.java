package com.kubou.application.service;

import com.kubou.domain.entity.GameSession;
import com.kubou.domain.entity.Player;
import com.kubou.domain.entity.Team;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TeamService {

    public void initializeTeams(GameSession session, int numberOfTeams) {
        session.getTeams().clear();
        for (int i = 1; i <= numberOfTeams; i++) {
            Team team = new Team(UUID.randomUUID().toString(), "Team " + i);
            session.getTeams().add(team);
        }
    }

    public void assignTeams(GameSession session) {
        if (session.getTeams().isEmpty()) {
            initializeTeams(session, 2); // Default to 2 teams if none exist
        }

        // Clear existing assignments to ensure clean slate/rebalance
        for (Team team : session.getTeams()) {
            team.getPlayerIds().clear();
        }

        List<Player> players = new ArrayList<>(session.getPlayers());
        // Shuffle players for random assignment
        Collections.shuffle(players);

        int teamCount = session.getTeams().size();
        for (int i = 0; i < players.size(); i++) {
            Team team = session.getTeams().get(i % teamCount);
            team.addPlayer(players.get(i).getId());
        }
    }

    public void balanceTeams(GameSession session) {
        // Since assignTeams already distributes round-robin (perfect balance),
        // this method can be a no-op or used for specific logic later.
        // For now, we'll leave it empty as assignTeams handles the balancing.
    }
}
