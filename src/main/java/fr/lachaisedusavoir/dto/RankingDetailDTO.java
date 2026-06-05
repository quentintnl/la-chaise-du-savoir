package fr.lachaisedusavoir.dto;

/**
 * DTO enrichi pour afficher les informations complètes de ranking d'un utilisateur.
 * Étend RankingDTO avec des statistiques supplémentaires.
 *
 * @param userId L'identifiant unique de l'utilisateur
 * @param userLogin Le login/pseudo de l'utilisateur
 * @param globalPoints Les points globaux accumulés
 * @param userWinstreak La série de victoires actuelle
 * @param rank Le rang dans le classement global
 */
public record RankingDetailDTO(
        Integer userId,
        String userLogin,
        Integer globalPoints,
        Integer userWinstreak,
        Integer rank
) {}

