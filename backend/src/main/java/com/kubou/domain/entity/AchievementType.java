package com.kubou.domain.entity;

public enum AchievementType {
    // Badges Temps Réel (Actifs)
    SNIPER("Sniper", "5 bonnes réponses d'affilée"),
    FLASH("Flash", "Réponse correcte en moins de 1 seconde"),
    TURBO("Turbo", "3 bonnes réponses rapides (< 5s) d'affilée"),
    COMEBACK("Comeback", "Bonne réponse après une erreur"),
    LUCKY("Lucky", "Bonne réponse sur une question difficile (Niveau 5)");

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
