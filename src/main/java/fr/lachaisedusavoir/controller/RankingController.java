package fr.lachaisedusavoir.controller;

import fr.lachaisedusavoir.dto.RankingDTO;
import fr.lachaisedusavoir.models.User;
import fr.lachaisedusavoir.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour les endpoints de ranking.
 * Gère les requêtes liées aux classements des utilisateurs.
 */
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    /**
     * Récupère le classement global de tous les utilisateurs.
     *
     * @return La liste des utilisateurs classés par points décroissants
     */
    @GetMapping("/global")
    public ResponseEntity<List<RankingDTO>> getGlobalRanking() {
        List<RankingDTO> rankings = rankingService.getGlobalRanking();
        return ResponseEntity.ok(rankings);
    }

    /**
     * Récupère le classement des utilisateurs par win streak.
     *
     * @return La liste des utilisateurs classés par win streak décroissant
     */
    @GetMapping("/winstreak")
    public ResponseEntity<List<RankingDTO>> getWinStreakRanking() {
        List<RankingDTO> rankings = rankingService.getWinStreakRanking();
        return ResponseEntity.ok(rankings);
    }

    /**
     * Récupère le rang d'un utilisateur spécifique.
     *
     * @param userId L'identifiant de l'utilisateur
     * @return Les informations de rang de l'utilisateur
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserRanking(@PathVariable Integer userId) {
        try {
            RankingDTO ranking = rankingService.getUserRanking(userId);
            return ResponseEntity.ok(ranking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur non trouvé avec l'ID: " + userId);
        }
    }

    /**
     * Ajoute des points à un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @param points Le nombre de points à ajouter
     * @return L'utilisateur mis à jour
     */
    @PostMapping("/user/{userId}/add-points")
    public ResponseEntity<?> addPoints(@PathVariable Integer userId, @RequestParam Integer points) {
        try {
            User user = rankingService.addPoints(userId, points);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    /**
     * Ajoute du win streak à un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @param streak Le nombre de victoires à ajouter
     * @return L'utilisateur mis à jour
     */
    @PostMapping("/user/{userId}/add-winstreak")
    public ResponseEntity<?> addWinStreak(@PathVariable Integer userId, @RequestParam Integer streak) {
        try {
            User user = rankingService.addWinStreak(userId, streak);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    /**
     * Réinitialise le win streak d'un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @return L'utilisateur mis à jour
     */
    @PostMapping("/user/{userId}/reset-winstreak")
    public ResponseEntity<?> resetWinStreak(@PathVariable Integer userId) {
        try {
            User user = rankingService.resetWinStreak(userId);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur non trouvé avec l'ID: " + userId);
        }
    }
}

