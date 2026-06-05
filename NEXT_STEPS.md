# 🚀 Prochaines étapes - La Chaise du Savoir

## ✅ Étape 1 : Intégration du Matching avec le Ranking - COMPLÉTÉE

Tout le travail suivant a été réalisé :
- ✅ Endpoints pour finaliser les matchs
- ✅ Affichage des résultats (gagnant/perdant)
- ✅ Enregistrement des points dans le ranking
- ✅ Tests unitaires complets
- ✅ Documentation

## 🔄 Étape 2 : Tests d'intégration (À faire)

### 2.1 Tester le flux complet
```bash
# Démarrer l'application
mvn spring-boot:run

# Ou avec Docker
docker-compose up

# Tests complets
mvn test

# Tests avec coverage
mvn test jacoco:report
```

### 2.2 Scénarios à tester
1. **Création et participation au match**
   - [ ] Vérifier que le code d'invitation fonctionne
   - [ ] Vérifier que `status` passe à true quand 2 joueurs
   - [ ] Vérifier que le match ne peut pas être rejoins deux fois

2. **Finalisation du match**
   - [ ] Vérifier que les points sont correctement calculés
   - [ ] Vérifier que le ranking global est mis à jour
   - [ ] Vérifier que le win streak est mis à jour
   - [ ] Vérifier que `finalized` devient true

3. **Récupération des résultats**
   - [ ] Vérifier le DTO retourné contient tous les champs
   - [ ] Vérifier les cas : victoire, défaite, égalité
   - [ ] Vérifier les points gagnés sont corrects

4. **Historique**
   - [ ] Vérifier que les matchs s'affichent correctement
   - [ ] Vérifier le filtrage des matchs terminés
   - [ ] Vérifier la pagination si applicable

## 📊 Étape 3 : Base de données

### 3.1 Appliquer les migrations SQL

Les fichiers suivants ont été créés et doivent être exécutés :
- `src/main/BDD/game_match_bdd.sql`
- `src/main/BDD/rounds_bdd.sql`
- `src/main/BDD/round_answer_bdd.sql`

Options d'intégration :
1. **Flyway** (recommandé)
   ```xml
   <dependency>
       <groupId>org.flywaydb</groupId>
       <artifactId>flyway-core</artifactId>
   </dependency>
   ```
   - Placer les migrations dans `src/main/resources/db/migration/`
   - Flyway les exécutera automatiquement

2. **Liquibase**
   ```xml
   <dependency>
       <groupId>org.liquibase</groupId>
       <artifactId>liquibase-core</artifactId>
   </dependency>
   ```

3. **Manuel**
   - Exécuter les scripts SQL directement dans MySQL

### 3.2 Vérifier la structure
```sql
-- Vérifier les tables
SHOW TABLES;
DESCRIBE game_match;
DESCRIBE rounds;
DESCRIBE round_answer;

-- Vérifier les données de test
SELECT * FROM game_match;
SELECT * FROM rounds;
SELECT * FROM round_answer;
```

## 🎨 Étape 4 : Frontend

### 4.1 Page de résultats de match

```javascript
// Affichage après finalisation
function displayMatchResults(matchResultDTO) {
  const { user1Login, user2Login, user1Score, user2Score, 
          user1PointsGained, user2PointsGained, winnerId, winnerLogin } = matchResultDTO;

  if (winnerId === null) {
    // Afficher l'égalité
    console.log(`🤝 Égalité entre ${user1Login} et ${user2Login}!`);
  } else {
    // Afficher le gagnant
    console.log(`🏆 ${winnerLogin} gagne le match!`);
  }

  // Afficher les scores
  console.log(`${user1Login}: ${user1Score} bonnes réponses - +${user1PointsGained} points`);
  console.log(`${user2Login}: ${user2Score} bonnes réponses - +${user2PointsGained} points`);
}
```

### 4.2 Interface suggestions
1. **Page de résultats**
   - Affiche le gagnant de manière évidente
   - Affiche les scores comparés
   - Affiche les points gagnés
   - Options : "Revoir le match", "Jouer à nouveau", "Accueil"

2. **Historique des matchs**
   - Liste des matchs terminés
   - Affiche l'adversaire, la date, le résultat
   - Filtre par statut (en cours, terminé, défaites)
   - Lien pour voir les détails

3. **Vidéo/Animation**
   - Animation d'annonce du gagnant
   - Confetti si l'utilisateur gagne
   - Animation de montée des points

## 📈 Étape 5 : Métriques et statistiques avancées

### 5.1 Nouvelles métriques à ajouter

#### Statistiques utilisateur dans le modèle User
```java
@Column(name = "total_matches_played")
private Integer totalMatchesPlayed = 0;

@Column(name = "matches_won")
private Integer matchesWon = 0;

@Column(name = "win_rate")
private Double winRate = 0.0; // calcul: matchesWon / totalMatchesPlayed

@Column(name = "average_score")
private Double averageScore = 0.0;
```

#### Nouveaux endpoints
```
GET /api/ranking/top-10          # Top 10 global
GET /api/ranking/users/{id}/stats # Statistiques détaillées
GET /api/match/user/{id}/history  # Historique complet
GET /api/match/stats/global       # Statistiques globales
```

### 5.2 Tableau de bord utilisateur
- Win rate
- Moyenne de points par match
- Nombre de victoires consécutives
- Adversaires favoris
- Matchs récents

## 🔐 Étape 6 : Sécurité

### 6.1 Vérifications à faire
- [ ] Authentification JWT sur tous les endpoints
- [ ] Autorisation : un joueur ne peut voir que ses propres matchs
- [ ] Validation des paramètres d'entrée
- [ ] Protection contre les attaques (SQL injection, XSS)
- [ ] Rate limiting sur les endpoints

### 6.2 À implémenter
```java
// Vérifier que l'utilisateur connecté peut accéder au match
@PreAuthorize("hasRole('USER')")
public boolean userIsInMatch(Integer matchId, Integer userId) {
    GameMatch match = gameMatchRepository.findById(matchId).orElseThrow();
    return match.getUser1().getId().equals(userId) || 
           match.getUser2().getId().equals(userId);
}
```

## 🧪 Étape 7 : Tests complète

### 7.1 Tests à écrire
- [ ] Tests d'intégration pour tout le flux
- [ ] Tests de sécurité (authentification, autorisation)
- [ ] Tests de charge (plusieurs matchs simultanés)
- [ ] Tests de concurrence (deux finalisations en même temps)

### 7.2 Couverture de code
- [ ] Viser au minimum 80% de couverture
- [ ] Utiliser Jacoco pour générer les rapports
```bash
mvn test jacoco:report
# Rapport JSON : target/site/jacoco/index.html
```

## 📚 Étape 8 : Documentation pour production

### 8.1 README.md
```markdown
# La Chaise du Savoir

## Déploiement
## Configuration
## Endpoints
## Tests
## Troubleshooting
```

### 8.2 Swagger/OpenAPI
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.0.2</version>
</dependency>
```

### 8.3 Wiki projet
- Architecture générale
- Flux utilisateur
- Configuration requise
- Guide de développement

## 🎯 Priorités

### 🔴 Haute priorité (Urgent)
1. [ ] Tests d'intégration du flux complet
2. [ ] Exécuter les migrations SQL
3. [ ] Tester les endpoints en production

### 🟡 Moyenne priorité (Important)
4. [ ] Développer l'interface frontend
5. [ ] Ajouter les statistiques utilisateur
6. [ ] Implémenter les métriques

### 🟢 Basse priorité (Nice to have)
7. [ ] Optimisations de performance
8. [ ] Cache du ranking
9. [ ] Websocket pour les mises à jour en temps réel

## 📅 Timeline estimée

| Phase | Tâchee | Durée |
|-------|--------|-------|
| 1 | Intégration matching-ranking | ✅ FAIT |
| 2 | Tests d'intégration | 2-3 jours |
| 3 | Migrations BD | 1 jour |
| 4 | Développement UI | 3-5 jours |
| 5 | Statistiques avancées | 2-3 jours |
| 6 | Sécurité et optimisation | 2 jours |
| 7 | Tests complète et déploiement | 2 jours |
| | **TOTAL** | **~2-3 semaines** |

## 🔗 Liens rapides

- **Documentation globale** : `MATCHING_RANKING_INTEGRATION.md`
- **Guide d'API** : `API_USAGE_GUIDE.md`
- **Résumé des changements** : `CHANGES_SUMMARY.md`
- **Index des fichiers** : `FILE_INDEX.md`
- **Tests unitaires** : `src/test/java/fr/lachaisedusavoir/service/MatchResultServiceTest.java`

## ⚙️ Configuration recommandée

```properties
# application.properties
# Logging
logging.level.fr.lachaisedusavoir=DEBUG
logging.level.org.springframework.web=INFO
logging.level.org.hibernate=DEBUG

# JPA
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.hibernate.ddl-auto=validate

# Actuator (pour la métriques)
management.endpoints.web.exposure.include=health,metrics,prometheus
```

## 🆘 Dépannage courant

### Problème : "Match is already finalized"
**Solution** : Un match ne peut être finalisé qu'une fois. Cette erreur indique que l'endpoint a déjà été appelé.

### Problème : "Match not found"
**Solution** : Vérifier que l'ID du match est correct et que le match existe dans la base de données.

### Problème : Points incorrects
**Solution** : Vérifier que `RoundAnswer.correct_answers` est correctement enregistré pour chaque joueur.

### Problème : Ranking non mis à jour
**Solution** : Vérifier que `RankingIntegrationService` est correctement injecté et que `recordWin`/`recordLoss`/`recordTie` sont appelés.

---

**Dernière mise à jour** : 5 Juin 2025  
**Statut** : Intégration matchmaking-ranking ✅ Complète

