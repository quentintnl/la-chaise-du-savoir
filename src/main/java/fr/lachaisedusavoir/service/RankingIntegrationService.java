package fr.lachaisedusavoir.service;

import fr.lachaisedusavoir.models.Session;
import fr.lachaisedusavoir.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service d'intégration du ranking avec le système d'authentification.
 * Gère l'initialisation et la mise à jour des statistiques de ranking
 * lors des événements d'authentification et de jeu.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RankingIntegrationService {

    private final RankingService rankingService;
    private final AuthService authService;

    /**
     * Initialise les statistiques de ranking pour un nouvel utilisateur.
     * Appelée après une inscription réussie.
     *
     * @param user L'utilisateur nouvellement créé
     */
    public void initializeRankingForNewUser(User user) {
        log.info("Initialisation du ranking pour l'utilisateur: {}", user.getLogin());
        // Les points et win streak sont déjà initialisés à 0 dans le constructeur User
        // Cette méthode peut être étendue à l'avenir pour ajouter des bonus d'inscription
    }

    /**
     * Enregistre une victoire pour un utilisateur en mode solo.
     * Ajoute des points et augmente le win streak.
     *
     * @param userId L'ID de l'utilisateur
     * @param points Les points gagnés pour cette victoire
     */
    public void recordWin(Integer userId, Integer points) {
        log.info("Victoire enregistrée pour l'utilisateur {}: {} points", userId, points);
        rankingService.addPoints(userId, points);
        rankingService.addWinStreak(userId, 1);
    }

    /**
     * Enregistre une défaite pour un utilisateur en mode solo.
     * Réinitialise le win streak et optionnellement pénalise des points.
     *
     * @param userId L'ID de l'utilisateur
     * @param penaltyPoints Les points de pénalité (peut être 0)
     */
    public void recordLoss(Integer userId, Integer penaltyPoints) {
        log.info("Défaite enregistrée pour l'utilisateur {}: pénalité de {} points", userId, penaltyPoints);
        rankingService.resetWinStreak(userId);
        if (penaltyPoints > 0) {
            // Note: Vous pouvez ajouter une méthode removePoints si nécessaire
            // Pour l'instant, les points ne sont pas retranchés en cas de défaite
            log.debug("Pénalité appliquée: {} points", penaltyPoints);
        }
    }

    /**
     * Enregistre une série de victoires en ajoutant des points bonus.
     * Par exemple: 5 victoires d'affilée = bonus de 25 points.
     *
     * @param userId L'ID de l'utilisateur
     * @param bonusMultiplier Le multiplicateur de bonus
     */
    public void applyWinStreakBonus(Integer userId, Integer bonusMultiplier) {
        User user = getUser(userId);
        Integer currentStreak = user.getUserWinstreak() != null ? user.getUserWinstreak() : 0;

        if (currentStreak > 0 && currentStreak % 5 == 0) {
            // Bonus tous les 5 victoires
            Integer bonusPoints = 25 * bonusMultiplier;
            log.info("Bonus de win streak appliqué à l'utilisateur {}: {} points", userId, bonusPoints);
            rankingService.addPoints(userId, bonusPoints);
        }
    }

    /**
     * Récupère un utilisateur par son ID.
     * Utilitaire interne.
     *
     * @param userId L'ID de l'utilisateur
     * @return L'utilisateur
     * @throws IllegalArgumentException si l'utilisateur n'existe pas
     */
    private User getUser(Integer userId) {
        return authService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));
    }
}

