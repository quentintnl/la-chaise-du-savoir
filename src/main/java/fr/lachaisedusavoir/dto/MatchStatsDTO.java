package fr.lachaisedusavoir.dto;

/**
 * DTO pour afficher les statistiques d'un match.
 * Utilisé pour afficher les détails complets d'un match avant sa finalisation.
 */
public record MatchStatsDTO(
        Integer matchId,
        Integer user1Id,
        String user1Login,
        Integer user2Id,
        String user2Login,
        Integer roundCount,
        String matchStatus
) {}

