package fr.lachaisedusavoir.dto;

/**
 * DTO pour représenter le résultat d'un match.
 * Contient les informations des deux joueurs et leur score.
 */
public record MatchResultDTO(
        Integer matchId,
        Integer user1Id,
        String user1Login,
        Integer user1Score,
        Integer user1PointsGained,
        Integer user2Id,
        String user2Login,
        Integer user2Score,
        Integer user2PointsGained,
        Integer winnerId,
        String winnerLogin
) {}

