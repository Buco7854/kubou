package com.kubou.domain.entity;

public enum AchievementType {
    SNIPER("Sniper", "5 bonnes réponses d'affilée"),
    FLASH("Flash", "Réponse correcte en moins de 1 seconde"),
    MARATHON("Marathonien", "Terminer un quiz complet"),
    FIRST_GAME("First Game", "Terminer sa première partie"),
    WINNER("Winner", "Finir premier"),
    TOP_3("Top 3", "Finir dans le top 3"),
    PERFECT("Perfect", "100% bonnes réponses");

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
