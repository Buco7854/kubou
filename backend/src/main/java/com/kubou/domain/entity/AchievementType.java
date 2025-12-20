package com.kubou.domain.entity;

public enum AchievementType {
    SNIPER("Sniper", "5 bonnes réponses d'affilée"),
    FLASH("Flash", "Réponse correcte en moins de 1 seconde"),
    MARATHON("Marathonien", "Terminer un quiz complet");

    private final String name;
    private final String description;

    AchievementType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
