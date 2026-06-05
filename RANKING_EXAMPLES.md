# Exemples de Requêtes API - Système de Ranking

## 📌 Prérequis

- L'application doit être en cours d'exécution sur `http://localhost:8080`
- Pour les endpoints avec authentification, vous devez d'abord vous connecter et obtenir un JWT token

## 🔐 Authentification

### 1. S'inscrire
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "login": "player_alpha",
    "password": "password123"
  }'
```

**Réponse** :
```json
{
  "success": true,
  "userId": 1,
  "login": "player_alpha",
  "apiToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 2. Se Connecter
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "login": "player_alpha",
    "password": "password123"
  }'
```

---

## 🏆 Examples - Ranking Endpoints

### 1. Obtenir le Classement Global Complet
```bash
curl http://localhost:8080/api/ranking/global \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Réponse** :
```json
[
  {
    "userId": 1,
    "userLogin": "player_alpha",
    "score": 350,
    "rank": 1
  },
  {
    "userId": 3,
    "userLogin": "player_gamma",
    "score": 250,
    "rank": 2
  },
  {
    "userId": 2,
    "userLogin": "player_beta",
    "score": 200,
    "rank": 3
  }
]
```

### 2. Obtenir le Top 5 des Joueurs
```bash
curl http://localhost:8080/api/ranking/global/top?limit=5 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 3. Obtenir le Classement par Win Streak
```bash
curl http://localhost:8080/api/ranking/winstreak \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 4. Obtenir le Rang d'un Joueur Spécifique
```bash
curl http://localhost:8080/api/ranking/user/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Réponse** :
```json
{
  "userId": 1,
  "userLogin": "player_alpha",
  "score": 350,
  "rank": 1
}
```

### 5. Ajouter des Points (Admin)
```bash
curl -X POST http://localhost:8080/api/ranking/user/1/add-points?points=50 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 6. Ajouter Win Streak (Admin)
```bash
curl -X POST http://localhost:8080/api/ranking/user/1/add-winstreak?streak=3 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 7. Réinitialiser Win Streak (Admin)
```bash
curl -X POST http://localhost:8080/api/ranking/user/1/reset-winstreak \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 🎮 Exemples - Match Endpoints

### 1. Créer un Match
```bash
curl -X POST http://localhost:8080/api/match/create \
  -H "Authorization: Bearer TOKEN_JOUEUR_A"
```

**Réponse** :
```json
{
  "id": 42,
  "inviteCode": "1234",
  "status": false,
  "message": "Partie créée avec succès. Partagez le code d'invitation."
}
```

### 2. Rejoindre un Match
```bash
curl -X POST http://localhost:8080/api/match/join?inviteCode=1234 \
  -H "Authorization: Bearer TOKEN_JOUEUR_B"
```

**Réponse** :
```json
{
  "id": 42,
  "inviteCode": "1234",
  "status": true,
  "message": "Vous avez rejoint la partie avec succès."
}
```

### 3. Récupérer un Match
```bash
curl http://localhost:8080/api/match/42
```

**Réponse** :
```json
{
  "id": 42,
  "user1": {
    "id": 1,
    "login": "player_alpha"
  },
  "user2": {
    "id": 2,
    "login": "player_beta"
  },
  "inviteCode": "1234",
  "status": true,
  "created_at": "2026-04-30T10:15:00"
}
```

### 4. Récupérer Tous les Matchs d'un Joueur
```bash
curl http://localhost:8080/api/match/user/matches \
  -H "Authorization: Bearer TOKEN"
```

### 5. Récupérer les Matchs en Attente
```bash
curl http://localhost:8080/api/match/user/pending \
  -H "Authorization: Bearer TOKEN"
```

### 6. Récupérer les Stats d'un Match (avant finalisation)
```bash
curl http://localhost:8080/api/match/42/stats
```

**Réponse** :
```json
{
  "matchId": 42,
  "user1Id": 1,
  "user1Login": "player_alpha",
  "user1Score": 8,
  "user1PointsGained": 0,
  "user2Id": 2,
  "user2Login": "player_beta",
  "user2Score": 7,
  "user2PointsGained": 0,
  "winnerId": null,
  "winnerLogin": "ONGOING"
}
```

### 7. Finaliser un Match ⭐ (Après les rounds)
```bash
curl -X POST http://localhost:8080/api/match/42/finalize
```

**Réponse** :
```json
{
  "matchId": 42,
  "user1Id": 1,
  "user1Login": "player_alpha",
  "user1Score": 8,
  "user1PointsGained": 102,
  "user2Id": 2,
  "user2Login": "player_beta",
  "user2Score": 7,
  "user2PointsGained": 22,
  "winnerId": 1,
  "winnerLogin": "player_alpha"
}
```

---

## 📊 Scénario Complet de Test

### Étape 1 : Créer 3 comptes

```bash
# Créer player_alpha
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"login": "player_alpha", "password": "pass123"}'

# Créer player_beta
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"login": "player_beta", "password": "pass123"}'

# Créer player_gamma
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"login": "player_gamma", "password": "pass123"}'
```

### Étape 2 : Ajouter des points pour voir le classement

```bash
# Ajouter 100 points à player_alpha
curl -X POST http://localhost:8080/api/ranking/user/1/add-points?points=100

# Ajouter 80 points à player_beta
curl -X POST http://localhost:8080/api/ranking/user/2/add-points?points=80

# Ajouter 120 points à player_gamma
curl -X POST http://localhost:8080/api/ranking/user/3/add-points?points=120
```

### Étape 3 : Voir le classement

```bash
curl http://localhost:8080/api/ranking/global
```

**Réponse** :
```json
[
  {
    "userId": 3,
    "userLogin": "player_gamma",
    "score": 120,
    "rank": 1
  },
  {
    "userId": 1,
    "userLogin": "player_alpha",
    "score": 100,
    "rank": 2
  },
  {
    "userId": 2,
    "userLogin": "player_beta",
    "score": 80,
    "rank": 3
  }
]
```

### Étape 4 : Créer et jouer un match

```bash
# Player A crée un match (token de player_alpha)
curl -X POST http://localhost:8080/api/match/create \
  -H "Authorization: Bearer TOKEN_ALPHA"

# → Reçoit code "1234"

# Player B rejoint (token de player_beta)
curl -X POST http://localhost:8080/api/match/join?inviteCode=1234 \
  -H "Authorization: Bearer TOKEN_BETA"

# [Les joueurs jouent les rounds...]

# Vérifier les stats actuelles
curl http://localhost:8080/api/match/1/stats

# Finaliser le match
curl -X POST http://localhost:8080/api/match/1/finalize

# [Les points sont maintenant mis à jour dans le ranking]

# Voir le nouveau classement
curl http://localhost:8080/api/ranking/global
```

---

## 🧪 Tests avec Postman

### Import les variables dans Postman

```json
{
  "baseUrl": "http://localhost:8080",
  "token_alpha": "",
  "token_beta": "",
  "matchId": ""
}
```

### Collection Postman

Voir `RANKING_POSTMAN_COLLECTION.json` for a complete Postman collection.

---

## 🐛 Dépannage

### Erreur : "User not found"
- Vérifiez que le userId existe
- Créez d'abord un compte avec /api/auth/signup

### Erreur : "Match not found"
- Vérifiez que le matchId est correct
- Le match doit exister avant de pouvoir le finaliser

### Erreur : "Match is already full"
- Le deuxième joueur a déjà rejoint
- Créez un nouveau match

### Erreur : "Invalid token"
- Vérifiez que le JWT token est valide
- Reconnectez-vous avec /api/auth/login
- Ajoutez `Authorization: Bearer TOKEN` dans les headers

---

## 📞 Support

Pour toute question sur le système de ranking, consultez :
- `RANKING_SYSTEM.md` - Documentation complète
- Les tests dans `src/test/java/fr/lachaisedusavoir/`

