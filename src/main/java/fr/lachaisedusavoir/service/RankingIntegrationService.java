package fr.lachaisedusavoir.service;

import fr.lachaisedusavoir.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service d'intégration du ranking avec le système d'authentification.
 * Gère l'initialisation et la mise à jour des statistiques de ranking
 * lors des événements d'authentification et de jeu.
 */
@Service
@RequiredArgsConstructor
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
         rankingService.resetWinStreak(userId);
         if (penaltyPoints > 0) {
             // Note: Vous pouvez ajouter une méthode removePoints si nécessaire
             // Pour l'instant, les points ne sont pas retranchés en cas de défaite
         }
     }

     /**
      * Enregistre une égalité (tie) pour deux utilisateurs en mode multijoueur.
      * Ajoute des points bonus d'égalité et maintient le win streak.
      *
      * @param userId L'ID de l'utilisateur
      * @param tiePoints Les points gagnés lors de l'égalité
      */
     public void recordTie(Integer userId, Integer tiePoints) {
         rankingService.addPoints(userId, tiePoints);
         // Le win streak n'est pas augmenté ni réinitialisé en cas d'égalité
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
        return authService.getUserById(userId);
    }
}

