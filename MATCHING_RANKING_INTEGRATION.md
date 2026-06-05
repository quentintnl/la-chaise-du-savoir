# Intégration du Matching avec le Ranking

## Vue d'ensemble

Ce document décrit l'intégration du système de matching (gestion des matchs entre zwei joueurs) avec le système de ranking (gestion des points et statistiques).

## Architecture

### Modèles
- **GameMatch** : Représente un match entre deux joueurs
  - `id` : Identifiant unique
  - `user1` : Premier joueur
  - `user2` : Deuxième joueur (peut être null en attente)
  - `inviteCode` : Code à 4 chiffres pour rejoindre un match
  - `status` : Indique si le match est en cours (les deux joueurs sont présents)
  - `finalized` : Indique si le match a été finalisé et les résultats enregistrés

- **Rounds** : Représente un round d'un match
  - Lié à un GameMatch
  - Contient le numéro du round et le nombre total de questions

- **RoundAnswer** : Les réponses d'un joueur pendant un round
  - Lié à un Rounds
  - Contient l'ID du joueur et le nombre de réponses correctes

### Services

#### MatchService
- `createMatch(userId)` : Crée un nouveau match pour un utilisateur
- `joinMatch(inviteCode, userId)` : Permet à un autre utilisateur de rejoindre un match

#### MatchResultService
- `finalizeMatch(matchId)` : Finalise un match et calcule les résultats
  - Calcule le nombre de réponses correctes pour chaque joueur
  - Détermine le gagnant
  - Enregistre les points via RankingIntegrationService
  - Marque le match comme finalisé
  - Retourne un MatchResultDTO avec tous les détails

- `getMatchStats(matchId)` : Récupère les statistiques actuelles d'un match en cours
- `getUserMatches(userId)` : Récupère tous les matchs d'un utilisateur
- `getUserCompletedMatches(userId)` : Récupère les matchs terminés d'un utilisateur

#### RankingIntegrationService
- `recordWin(userId, points)` : Enregistre une victoire
  - Ajoute les points au ranking
  - Augmente le win streak
  
- `recordLoss(userId, penaltyPoints)` : Enregistre une défaite
  - Réinitialise le win streak
  
- `recordTie(userId, tiePoints)` : Enregistre une égalité
  - Ajoute les points
  - Maintient le win streak inchangé

- `applyWinStreakBonus(userId, bonusMultiplier)` : Applique un bonus tous les 5 victoires

### Calcul des points

#### Victoire
```
Points = 100 + (différence de score * 2)
```

#### Défaite
```
Points = 20 + max(0, score du perdant - 5)
```

#### Égalité
```
Points = 50
```

## Endpoints API

### Créer un match
```
POST /api/match/create
Authentication: JWT Token
```
**Réponse**: ID du match et code d'invitation

### Rejoindre un match
```
POST /api/match/join?inviteCode=XXXX
Authentication: JWT Token
```
**Réponse**: ID du match et statut

### Récupérer une question
```
GET /api/match/{matchId}/question
```
**Réponse**: Liste des questions

### Finaliser un match et afficher les résultats
```
POST /api/match/{matchId}/finalize
```
**Réponse (MatchResultDTO)**:
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

### Récupérer les statistiques d'un match
```
GET /api/match/{matchId}/stats
```
**Réponse**: Statistiques actuelles du match (sans points finalisés)

### Récupérer tous les matchs d'un utilisateur
```
GET /api/match/user/all
Authentication: JWT Token
```
**Réponse**: Liste de tous les matchs

### Récupérer les matchs terminés
```
GET /api/match/user/completed
Authentication: JWT Token
```
**Réponse**: Liste des matchs terminés et finalisés

## Flux complet d'un match

1. **Création** : Un joueur crée un match → Reçoit un code d'invitation
2. **Adhésion** : Un autre joueur rejoint avec le code
3. **Jeu** : Les deux joueurs répondent aux questions et leurs réponses sont enregistrées dans les tables de Rounds/RoundAnswer
4. **Finalisation** : Le match est finalisé via l'endpoint `/api/match/{matchId}/finalize`
   - Les scores sont calculés
   - Le gagnant est déterminé
   - Les points sont enregistrés dans le système de ranking
   - Les statistiques de l'utilisateur sont mises à jour
5. **Résultats** : Le client affiche les résultats avec gagnant et perdant

## Intégration avec le Ranking

L'intégration se fait via `RankingIntegrationService` qui :
1. Enregistre les victoires/défaites/égalités
2. Ajoute les points appropriés
3. Gère le win streak
4. Applique les bonus automatiques

Les points sont persistés dans la base de données utilisateur (modèle User) et reflétés dans le classement global.

## Structure de base de données

### game_match table
```sql
CREATE TABLE `game_match` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user1_id` INT NOT NULL,
    `user2_id` INT,
    `invite_code` VARCHAR(4),
    `created_at` DATETIME NOT NULL,
    `status` BOOLEAN,
    `finalized` BOOLEAN NOT NULL DEFAULT FALSE,
    ...
);
```

### rounds table
```sql
CREATE TABLE `rounds` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `match_id` INT NOT NULL,
    `round_number` INT,
    `total_question` INT,
    ...
);
```

### round_answer table
```sql
CREATE TABLE `round_answer` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `round_id` INT NOT NULL,
    `player_id` INT NOT NULL,
    `correct_answers` INT NOT NULL,
    ...
);
```

## Tests

Voir `TESTING_GUIDE.md` pour les détails sur le test du système de matching et ranking.

## Notes importantes

- Un match ne peut être finalisé que s'il a au moins 2 joueurs (`status = true`)
- Un match ne peut être finalisé qu'une seule fois
- Les points sont uniquement enregistrés lors de la finalisation, pas à chaque round
- Le système gère automatiquement les trois cas : victoire, défaite et égalité
- Le win streak est maintenu séparément du score et peut influencer les bonus futurs

