package fr.lachaisedusavoir.dto;

/**
 * DTO pour les informations de ranking d'un utilisateur.
 *
 * @param userId L'identifiant unique de l'utilisateur
 * @param userLogin Le login de l'utilisateur
 * @param score Le score (points globaux ou win streak selon le classement)
 * @param rank Le rang de l'utilisateur dans le classement (1-indexed)
 */
public record RankingDTO(
        Integer userId,
        String userLogin,
        Integer score,
        Integer rank
) {}

