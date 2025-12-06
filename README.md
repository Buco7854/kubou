# 🦉 Kubou

[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Buco7854_kubou&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=Buco7854_kubou)

> **Learning made Fun.**
> Une plateforme de quiz interactive type Kahoot/Wooclap, avec un design engageant et une architecture moderne.

---

## 🛠️ Prérequis

Avant de commencer, assurez-vous d'avoir les éléments suivants sur votre machine :
* **Java 21** (Minimum requis)
* **Docker** & **Docker Compose**

## 🚀 Lancer le projet (Mode Dev)

Voici comment démarrer l'environnement de développement local en deux étapes.

### 1. Initialiser l'infrastructure (Base de données)
Lancez les conteneurs nécessaires (PostgreSQL, etc.) en tâche de fond :

```bash
docker-compose -f docker-compose.dev.yml up -d
```
2. Démarrer le Backend
   Une fois la base de données prête, lancez l'application Spring Boot avec le profil de développement :
```bash
cd backend
./gradlew bootRun -D spring.profiles.active=dev
```

> 💡 Note : L'application se lancera avec le rechargement à chaud (si configuré) et les logs détaillés.
>
🔗 Documentation & Accès
Une fois le serveur démarré, vous avez accès aux outils suivants :
| Service | URL |
|---|---|
| Swagger UI (API Docs) | http://localhost:8080/swagger-ui/index.html |
| API JSON | http://localhost:8080/swagger-ui/v3/api-docs |

