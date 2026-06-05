# Système de Ranking Overall - Documentation

## Vue d'ensemble

Le système de ranking overall est un classement généraliste de tous les joueurs de l'application basé sur les **points globaux** accumulés. C'est un système simple et transparent où :
- Plus vous avez de points = Plus vous êtes bien classé
- Les points sont gagnés à travers les victoires aux matchs
- Un win streak (série de victoires) est suivi séparément

## 🏆 Architecture du Ranking

### 1. Modèles Principaux

#### User (Utilisateur)
```java
- id: Integer (clé primaire)
- login: String (pseudonyme unique)
- password: String (hashé)
- globalPoints: Integer (points globaux) - **utilisé pour le ranking**
- userWinstreak: Integer (série de victoires actuelle)
```

#### GameMatch (Match)
```java
- id: Integer
- user1: User (créateur du match)
- user2: User (second joueur)
- inviteCode: String (code 4 chiffres unique)
- created_at: Date
- status: Boolean (false=en attente, true=en cours/terminé)
```

#### Rounds & RoundAnswer
```java
- Rounds: Contient les rounds d'un match
- RoundAnswer: Chaque joueur fournit ses réponses correctes par round
```

### 2. Flux de Calcul des Points

#### Avant un Match
```
User A (100 pts) vs User B (150 pts)
```

#### Pendant le Match
- Chaque joueur joue 3 rounds
- Chaque round a X questions
- Les réponses correctes sont enregistrées

#### Après le Match - Finalisation
```
User A : 8 réponses correctes (2+3+3)
User B : 7 réponses correctes (2+2+3)

Gagnant : User A (8 > 7)
Points gagnés :
  - User A (gagnant) : 100 + (différence * 2) = 100 + (8-7)*2 = 102 points
  - User B (perdant) : 20 + max(0, 7-5) = 20 + 2 = 22 points
  
En cas d'égalité : Chacun reçoit 50 points
```

### 3. Formula de Calcul des Points

#### Victoire
```
basPoints + (scoreDifference * 2)
= 100 + ((correctAnswersWinner - correctAnswersLoser) * 2)
```

#### Défaite (Consolation)
```
consolePoints + max(0, loserScore - 5)
= 20 + max(0, correctAnswersLoser - 5)
```

#### Égalité
```
50 points pour chaque joueur
```

### 4. Win Streak (Série de Victoires)

Le win streak est réinitialisé à 0 après chaque défaite :
- Victoire → +1 win streak
- Défaite → reset à 0
- Égalité → reset à 0

Bonus à chaque 5 victoires : +25 points bonus

---

## 📋 Endpoints API

### Ranking Endpoints

#### 1. Classement Global Complet
```http
GET /api/ranking/global
```
**Réponse** : Liste de tous les utilisateurs classés par points (décroissant)
```json
[
  {
    "userId": 1,
    "userLogin": "player_alpha",
    "score": 350,
    "rank": 1
  },
  {
    "userId": 2,
    "userLogin": "player_beta",
    "score": 200,
    "rank": 2
  }
]
```

#### 2. Top 10 Utilisateurs par Points
```http
GET /api/ranking/global/top?limit=10
```
**Paramètres** :
- `limit` (optionnel, défaut=10, max=100) : nombre de résultats

#### 3. Classement par Win Streak
```http
GET /api/ranking/winstreak
```
**Réponse** : Liste classée par série de victoires

#### 4. Top 10 par Win Streak
```http
GET /api/ranking/winstreak/top?limit=10
```

#### 5. Rang d'un Utilisateur Spécifique
```http
GET /api/ranking/user/{userId}
```
**Exemple** :
```http
GET /api/ranking/user/1
```

#### 6. Ajouter des Points (Admin/Test)
```http
POST /api/ranking/user/{userId}/add-points?points=50
```

#### 7. Ajouter Win Streak (Admin/Test)
```http
POST /api/ranking/user/{userId}/add-winstreak?streak=3
```

#### 8. Réinitialiser Win Streak (Admin/Test)
```http
POST /api/ranking/user/{userId}/reset-winstreak
```

---

### Match & Finalisation Endpoints

#### 1. Créer un Match
```http
POST /api/match/create
```
**Authentification** : Requise (utilisateur connecté)
```json
// Réponse
{
  "id": 42,
  "inviteCode": "1234",
  "status": false,
  "message": "Partie créée avec succès. Partagez le code d'invitation."
}
```

#### 2. Rejoindre un Match
```http
POST /api/match/join?inviteCode=1234
```
**Authentification** : Requise

#### 3. Récupérer un Match
```http
GET /api/match/{matchId}
```

#### 4. Récupérer Tous les Matchs d'un Utilisateur
```http
GET /api/match/user/matches
```
**Authentification** : Requise

#### 5. Récupérer les Matchs en Attente
```http
GET /api/match/user/pending
```
**Authentification** : Requise

#### 6. Récupérer les Statistiques d'un Match (avant finalisation)
```http
GET /api/match/{matchId}/stats
```
**Réponse** :
```json
{
  "matchId": 42,
  "user1Id": 1,
  "user1Login": "player1",
  "user1Score": 8,
  "user1PointsGained": 0,
  "user2Id": 2,
  "user2Login": "player2",
  "user2Score": 7,
  "user2PointsGained": 0,
  "winnerId": null,
  "winnerLogin": "ONGOING"
}
```

#### 7. Finaliser un Match ⭐
```http
POST /api/match/{matchId}/finalize
```
**Réponse** :
```json
{
  "matchId": 42,
  "user1Id": 1,
  "user1Login": "player1",
  "user1Score": 8,
  "user1PointsGained": 102,
  "user2Id": 2,
  "user2Login": "player2",
  "user2Score": 7,
  "user2PointsGained": 22,
  "winnerId": 1,
  "winnerLogin": "player1"
}
```

---

## 🔄 Flux d'Utilisation Complet

### Scénario : Deux joueurs font un match

```
1. Joueur A crée un match
   POST /api/match/create
   → Reçoit code d'invitation: "1234"

2. Joueur A partage le code avec Joueur B

3. Joueur B rejoint le match
   POST /api/match/join?inviteCode=1234
   → Les deux joueurs jouent les rounds

4. Après les 3 rounds, on peut vérifier les stats
   GET /api/match/42/stats
   → Jouer A: 8 réponses correctes
   → Joueur B: 7 réponses correctes

5. Finaliser le match (une seule fois!)
   POST /api/match/42/finalize
   → Joueur A gagne +102 points → ranking updated
   → Joueur B gagne +22 points → ranking updated
   → Win streak de A augmente de 1
   → Win streak de B se réinitialise à 0

6. Vérifier le nouveau classement
   GET /api/ranking/global
   → Joueur A a monté de place
```

---

## 💾 Base de Données - Schéma Minimal

```sql
-- Utilisateurs avec scores
SELECT id, login, global_points, user_winstreak 
FROM user 
ORDER BY global_points DESC;

-- Matchs d'un joueur
SELECT id, user1_id, user2_id, status, created_at 
FROM game_match 
WHERE user1_id = ? OR user2_id = ? 
ORDER BY created_at DESC;

-- Réponses d'un joueur dans un match
SELECT ra.player_id, ra.correct_answers, r.round_number
FROM round_answer ra
JOIN rounds r ON ra.round_id = r.id
WHERE r.match_id = ?
ORDER BY r.round_number;
```

---

## 🎯 Prochaines Étapes

Une fois que Quentin aura fait le matching (algorithme de pairing des joueurs) :

1. **Intégration du Matching** :
   - Créer des groupes de joueurs
   - Assigner automatiquement les matchs via le matching

2. **Amis & Classement Amis** (futur) :
   - Ajouter système de friends
   - Créer un classement parmi les amis

3. **Optimisations** :
   - Ajouter des indexes sur `globalPoints`, `userWinstreak`
   - Cache redis pour les classements très consultés
   - Pagination pour de grandes listes

4. **Analytics** :
   - Historique des matchs
   - Stats par joueur
   - Tendances (montées/descentes dans le ranking)

---

## 📝 Notes Importantes

- **Atomicité** : Les finalisations de match utilisent `@Transactional` pour garantir que les points sont attribués correctement
- **Éviter les doublons** : Ne pas appeler `finalize` deux fois sur le même match!
- **Codes uniques** : Les codes d'invitation sont générés de manière à être uniques
- **Perte de winstreak** : Même une égalité réinitialise le winstreak (à améliorer selon les règles métier)


