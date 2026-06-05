package fr.lachaisedusavoir.dto;

/**
 * DTO pour afficher les résultats d'un round pour un joueur.
 */
public record RoundResultDTO(
        Integer roundId,
        Integer roundNumber,
        Integer playerId,
        String playerLogin,
        Integer correctAnswers,
        Integer totalQuestions,
        Integer pointsEarned
) {}

