# Guide d'utilisation API - Matching & Ranking

## Flux complet d'un match

### 1️⃣ Joueur 1 crée un match

**Requête** :
```bash
curl -X POST http://localhost:8080/api/match/create \
  -H "Authorization: Bearer <JWT_TOKEN_USER1>"
```

**Réponse** :
```json
{
  "id": 1,
  "inviteCode": "4829",
  "status": false,
  "message": "Partie créée avec succès. Partagez le code d'invitation."
}
```

### 2️⃣ Joueur 2 rejoint le match

**Requête** :
```bash
curl -X POST http://localhost:8080/api/match/join?inviteCode=4829 \
  -H "Authorization: Bearer <JWT_TOKEN_USER2>"
```

**Réponse** :
```json
{
  "id": 1,
  "inviteCode": "4829",
  "status": true,
  "message": "Vous avez rejoint la partie avec succès."
}
```

### 3️⃣ Les joueurs récupèrent les questions

**Requête** (peut être fait par n'importe qui) :
```bash
curl -X GET http://localhost:8080/api/match/1/question \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Réponse** :
```json
[
  {
    "id": 1,
    "question": "Quelle est la capitale de la France?",
    "options": ["Paris", "Londres", "Berlin", "Madrid"],
    "correct": 0,
    "difficulty": "easy"
  },
  ...
]
```

### 4️⃣ Les joueurs jouent et enregistrent leurs réponses

**Via le frontend** : Les réponses sont enregistrées dans les tables `Rounds` et `RoundAnswer` après chaque round

Tables mises à jour :
- `Rounds` : Enregistre le numéro du round et le nombre de questions
- `RoundAnswer` : Enregistre la réponse du joueur avec son nombre de réponses correctes

### 5️⃣ Finaliser le match et afficher les résultats

**Requête** :
```bash
curl -X POST http://localhost:8080/api/match/1/finalize
```

**Réponse (MatchResultDTO)** :

#### Cas de victoire :
```json
{
  "matchId": 1,
  "user1Id": 1,
  "user1Login": "alice",
  "user1Score": 8,
  "user1PointsGained": 116,
  "user2Id": 2,
  "user2Login": "bob",
  "user2Score": 6,
  "user2PointsGained": 24,
  "winnerId": 1,
  "winnerLogin": "alice"
}
```

#### Cas d'égalité :
```json
{
  "matchId": 1,
  "user1Id": 1,
  "user1Login": "alice",
  "user1Score": 8,
  "user1PointsGained": 50,
  "user2Id": 2,
  "user2Login": "bob",
  "user2Score": 8,
  "user2PointsGained": 50,
  "winnerId": null,
  "winnerLogin": "TIE"
}
```

### 6️⃣ Afficher les statistiques avant finalisation (optionnel)

**Requête** :
```bash
curl -X GET http://localhost:8080/api/match/1/stats \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Réponse** (avant finalisation) :
```json
{
  "matchId": 1,
  "user1Id": 1,
  "user1Login": "alice",
  "user1Score": 8,
  "user1PointsGained": 0,
  "user2Id": 2,
  "user2Login": "bob",
  "user2Score": 6,
  "user2PointsGained": 0,
  "winnerId": null,
  "winnerLogin": "ONGOING"
}
```

### 7️⃣ Consulter l'historique des matchs

**Requête - Tous les matchs** :
```bash
curl -X GET http://localhost:8080/api/match/user/all \
  -H "Authorization: Bearer <JWT_TOKEN_USER1>"
```

**Réponse** :
```json
[
  {
    "id": 1,
    "user1": { "id": 1, "login": "alice" },
    "user2": { "id": 2, "login": "bob" },
    "inviteCode": "4829",
    "createdAt": "2025-06-05T10:30:00",
    "status": true,
    "finalized": true
  },
  {
    "id": 2,
    "user1": { "id": 1, "login": "alice" },
    "user2": null,
    "inviteCode": "5634",
    "createdAt": "2025-06-05T11:00:00",
    "status": false,
    "finalized": false
  }
]
```

**Requête - Matchs terminés uniquement** :
```bash
curl -X GET http://localhost:8080/api/match/user/completed \
  -H "Authorization: Bearer <JWT_TOKEN_USER1>"
```

## 📊 Affichage des résultats (Frontend)

### Exemple d'affichage après finalisation

```javascript
// Récupérer les résultats
const result = matchResultDTO;

// Affichage
if (result.winnerId === null) {
  // Cas d'égalité
  afficher(`🤝 Égalité entre ${result.user1Login} et ${result.user2Login}!`);
  afficher(`Chacun gagne ${result.user1PointsGained} points`);
} else {
  // Cas avec un gagnant
  if (result.winnerId === user1Id) {
    afficher(`🏆 ${result.user1Login} gagne le match!`);
    afficher(`${result.user1Login}: ${result.user1Score}/10 - +${result.user1PointsGained} pts`);
    afficher(`${result.user2Login}: ${result.user2Score}/10 - +${result.user2PointsGained} pts`);
  } else {
    afficher(`🏆 ${result.user2Login} gagne le match!`);
    afficher(`${result.user2Login}: ${result.user2Score}/10 - +${result.user2PointsGained} pts`);
    afficher(`${result.user1Login}: ${result.user1Score}/10 - +${result.user1PointsGained} pts`);
  }
}
```

## 🔍 Cas d'erreur

### Match non trouvé

**Requête** :
```bash
curl -X POST http://localhost:8080/api/match/999/finalize
```

**Réponse (400)** :
```json
"Match not found with ID: 999"
```

### Match incomplet (pas les deux joueurs)

**Réponse (400)** :
```json
"Match must have both players to be finalized"
```

### Match déjà finalisé

**Réponse (400)** :
```json
"Match is already finalized"
```

## 💾 Enregistrement du Ranking

Lors de la finalisation, le système automatiquement :

1. **Calcule les points** basés sur les scores
2. **Enregistre les points** dans la base de données utilisateur
3. **Met à jour le win streak**
4. **Applique les bonus** (si applicable)

Les points sont reflétés immédiatement dans :
- Le classement global (`/api/ranking/global`)
- Le classement par win streak (`/api/ranking/winstreak`)
- Les statistiques utilisateur

## 📈 Exemple complet en JavaScript

```javascript
async function playMatch(user1Token, user2Token) {
  // 1. Créer le match
  const createRes = await fetch('http://localhost:8080/api/match/create', {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${user1Token}` }
  });
  const { id: matchId, inviteCode } = await createRes.json();
  console.log(`Match créé avec le code: ${inviteCode}`);

  // 2. Rejoindre le match
  await fetch(`http://localhost:8080/api/match/join?inviteCode=${inviteCode}`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${user2Token}` }
  });
  console.log('Joueur 2 a rejoint le match');

  // 3. Les joueurs jouent... (frontend game logic)

  // 4. Finaliser et afficher les résultats
  const finalizeRes = await fetch(`http://localhost:8080/api/match/${matchId}/finalize`, {
    method: 'POST'
  });
  const results = await finalizeRes.json();
  
  console.log(`🏆 ${results.winnerLogin} a gagné !`);
  console.log(`${results.user1Login}: ${results.user1Score} pts (${results.user1PointsGained} gagnés)`);
  console.log(`${results.user2Login}: ${results.user2Score} pts (${results.user2PointsGained} gagnés)`);
}
```

## 🧪 Tests avec cURL

### Créer deux utilisateurs de test

```bash
# User 1
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"login":"alice","password":"pass123"}'

# User 2
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"login":"bob","password":"pass123"}'

# Se connecter et récupérer les tokens
ALICE_TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"alice","password":"pass123"}' | jq -r '.token')

BOB_TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"bob","password":"pass123"}' | jq -r '.token')
```

### Tester le flux

```bash
# Créer le match
INVITE_CODE=$(curl -X POST http://localhost:8080/api/match/create \
  -H "Authorization: Bearer $ALICE_TOKEN" | jq -r '.inviteCode')

# Rejoindre
curl -X POST "http://localhost:8080/api/match/join?inviteCode=$INVITE_CODE" \
  -H "Authorization: Bearer $BOB_TOKEN"

# Finaliser
curl -X POST http://localhost:8080/api/match/1/finalize
```

## 📝 Notes

- Les résultats du match sont persistés en base de données
- Les points du ranking sont mis à jour immédiatement
- Un match ne peut être finalisé qu'une fois
- Les statistiques du profil utilisateur sont mises à jour automatiquement

