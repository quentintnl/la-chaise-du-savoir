# La Chaise du Savoir
### Lancement en local

Ce projet est composé de trois services :
- **Backend** (Spring Boot + Maven)
- **Frontend** (Next.js)
- **Base de données** (MySQL)

L’ensemble est orchestré avec **Docker Compose**.

---

## Prérequis

Avant de commencer, assure-toi d’avoir installé :

- Docker Desktop → https://docs.docker.com/get-started/get-docker/
- Java 17
- Node.js 24
- npm 11

---

## Architecture des services

| Service | Description | Port |
|--------|-------------|------|
| db     | Base de données MySQL | 3306 |
| back   | API Spring Boot | 8080 |
| front  | Application Next.js | 3000 |

---

## Configuration des services

### Démarrer le projet
Place-toi à la racine du projet (là où se trouve compose.yml) puis exécute :

```docker compose up -d ```


## Accès aux services

Une fois démarré :

🖥️ Frontend → http://localhost:3000

⚙️ Backend → http://localhost:8080

🗄️ MySQL → localhost:3306