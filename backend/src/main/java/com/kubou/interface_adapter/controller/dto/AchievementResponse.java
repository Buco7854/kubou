package com.kubou.interface_adapter.controller.dto;

import com.kubou.domain.entity.PlayerAchievement;
import java.util.List;

public class AchievementResponse {
    private boolean isGuest;
    private String message;
    private List<PlayerAchievement> achievements;

    public AchievementResponse(boolean isGuest, String message, List<PlayerAchievement> achievements) {
        this.isGuest = isGuest;
        this.message = message;
        this.achievements = achievements;
    }

    public boolean isGuest() { return isGuest; }
    public void setGuest(boolean guest) { isGuest = guest; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<PlayerAchievement> getAchievements() { return achievements; }
    public void setAchievements(List<PlayerAchievement> achievements) { this.achievements = achievements; }
}
