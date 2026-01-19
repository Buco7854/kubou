# 🦉 Kubou - Plateforme d'Évaluation & Gamification

> **Learning made Fun & Data-Driven.**
> Kubou est une plateforme interactive d'apprentissage qui transforme l'évaluation en expérience gamifiée, tout en offrant aux enseignants des outils d'analyse paramétriques avancés.

[![CI/CD](https://github.com/Buco7854/kubou/actions/workflows/build.yml/badge.svg)](https://github.com/Buco7854/kubou/actions)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Buco7854_kubou&metric=alert_status)](https://sonarcloud.io/dashboard?id=Buco7854_kubou)

---

## 📋 Gestion de Projet

Le suivi des tâches et l'évolution du projet sont gérés via un tableau Kanban GitHub Projects.

👉 **[Accéder au Kanban du Projet](https://github.com/users/Buco7854/projects/2)**

---

## ✨ Fonctionnalités Clés (Core Features)

Le projet s'articule autour de 6 piliers majeurs :

### 1. 🔄 Cycle de Vie & Temps Réel
* Gestion complète des sessions de jeu (Lobby, Questions, Résultats).
* Calcul en temps réel des moyennes pondérées par utilisateur.
* Synchronisation instantanée via WebSockets.

### 2. 🧮 Moteur de Notation Hybride (Strategy Pattern)
* Système flexible permettant au professeur de définir sa propre formule de notation.
* **Critères pondérables :** Justesse, Temps de réponse (Vitesse), et Séries de victoires (Streak).
* Implémentation via le pattern *Strategy* pour injecter des règles de calcul dynamiques.

### 3. 📊 Analytique "Boîte Noire" (Parametric Analytics)
* Module de construction de graphiques à la demande.
* L'utilisateur fournit les paramètres d'entrée (Période, Sujet, Cible) et le système génère la visualisation adéquate.
* **Sorties :** Courbes d'évolution, Radar de compétences, Taux d'abandon/Rétention.

### 4. 🏆 Gamification Événementielle
* Système basé sur des *Event Listeners* pour débloquer des succès.
* **Badges exemples :** "Sniper" (5 réponses justes d'affilée), "Flash" (réponse < 1s).
* Renforcement positif pour maintenir l'engagement.

### 5. ⚔️ Mode Team Battle
* Compétition "Groupe contre Groupe".
* Agrégation des scores individuels pour calculer un score d'équipe unifié en temps réel.

### 6. 🧠 Smart Review (Révision Intelligente)
* Génération automatique de "Quiz de Rattrapage".
* Analyse l'historique des échecs pour proposer un contenu ciblé sur les lacunes de l'élève.

---

## 🏗️ Architecture Technique

Le projet respecte les principes de la **Clean Architecture** pour isoler la logique métier complexe (Scoring, Smart Review).

### Structure
* **`domain`**: Entités (Game, Player) et Interfaces de Services (Règles métier pures).
* **`application`**: Cas d'utilisation (Use Cases) orchestrant les flux.
* **`infrastructure`**: Implémentations (JPA, WebSocket, Security).
* **`interface_adapter`**: Contrôleurs REST et WebSocket.

### Stack Technologique
* **Backend:** Java 21, Spring Boot 3, Spring Security (JWT), WebSocket (STOMP).
* **Frontend:** Vue.js 3, TypeScript, TailwindCSS, Pinia.
* **Data:** PostgreSQL 18, Docker.

---

## 💻 Démarrage (Environnement de Dev)

### Prérequis
* Docker & Docker Compose
* Java 21 (optionnel si via Docker)
* Node.js 20+ (pour le frontend local)

### Lancer le projet

1.  **Lancer l'infrastructure (Base de données)**
    ```bash
    docker-compose -f docker-compose.dev.yml up -d
    ```

2.  **Lancer le Backend**
    ```bash
    cd backend
    ./gradlew bootRun
    ```
    *API Docs (Swagger):* http://localhost:8080/swagger-ui/index.html

3.  **Lancer le Frontend**
    ```bash
    cd frontend
    npm install
    npm run dev
    ```
    *Application:* http://localhost:5173

---

## 👤 Contributeurs
**[Insights]([https://github.com/Buco7854](https://github.com/Buco7854/kubou/graphs/contributors))**.
