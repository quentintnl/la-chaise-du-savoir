# 🐳 Documentation Docker - Backend

## Démarrage rapide

```bash
# Lancer tous les services (backend + base de données)
docker compose up -d

# Vérifier que le backend est actif
curl http://localhost:8080/api/health
```

## Services

### Backend (`back`)
- **Port**: `8080`
- **URL**: `http://localhost:8080`
- **Image**: `ghcr.io/quentintnl/la-chaise-du-savoir:latest`
- **Base de données**: `jdbc:mysql://db:3306/la-chaise-du-savoir`

### Base de données (`db`)
- **Port**: `3306`
- **Type**: MySQL
- **Identifiants**: `root` / `root`
- **Base**: `la-chaise-du-savoir`

## Commandes essentielles

```bash
# Démarrer les services
docker compose up -d

# Arrêter les services
docker compose down

# Voir les logs du backend
docker compose logs -f back

# Voir tous les logs
docker compose logs -f

# Redémarrer le backend
docker compose restart back

# Reconstruire le backend depuis le code local
docker compose up -d --build
```

## Configuration

### Environnement du backend

Les variables d'environnement sont définis dans `compose.yml`:

| Variable | Valeur |
|----------|--------|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://db:3306/la-chaise-du-savoir` |

### Identifiants par défaut

**Base de données**:
- Utilisateur: `root`
- Mot de passe: `root`

**JWT Secret** (changer en production):
- Voir `application.properties` ligne 22

## Ports exposés

- `8080` - Backend Spring Boot
- `3306` - MySQL Database
- `3000` - Frontend (si lancé)

## Réseau

Tous les services communiquent sur le réseau bridge `lcds-network`

## Dépannage

```bash
# Vérifier la connexion à la base de données
docker compose exec back curl -s http://back:8080/api/health

# Consulter les logs du conteneur backend
docker compose logs back --tail=100

# Supprimer les volumes (réinitialise la BD)
docker compose down -v
```

## Build personnalisé

Pour utiliser le Dockerfile local au lieu de l'image précompilée:

1. Éditer `compose.yml` ligne 15: décommenter `build: .` et commenter la ligne `image:`
2. Relancer: `docker compose up -d --build`

---

**Note**: Cette configuration est optimisée pour le développement. En production, changer:
- `MYSQL_ROOT_PASSWORD`
- `jwt.secret` dans `application.properties`

