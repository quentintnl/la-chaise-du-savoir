package fr.lachaisedusavoir.service;

import fr.lachaisedusavoir.dto.MatchResultDTO;
import fr.lachaisedusavoir.models.GameMatch;
import fr.lachaisedusavoir.models.RoundAnswer;
import fr.lachaisedusavoir.models.Rounds;
import fr.lachaisedusavoir.models.User;
import fr.lachaisedusavoir.repository.GameMatchRepository;
import fr.lachaisedusavoir.repository.RoundAnswerRepository;
import fr.lachaisedusavoir.repository.RoundsRepository;
import fr.lachaisedusavoir.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service pour gérer les résultats des matchs et le calcul des points.
 * Responsable de la finalisation des matchs et de l'attribution des points aux gagnants.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchResultService {

    private final GameMatchRepository gameMatchRepository;
    private final RoundsRepository roundsRepository;
    private final RoundAnswerRepository roundAnswerRepository;
    private final UserRepository userRepository;
    private final RankingIntegrationService rankingIntegrationService;

    /**
     * Finalise un match en calculant le gagnant et en attribuant les points.
     * Le gagnant est celui avec le plus de réponses correctes au total.
     *
     * @param matchId L'ID du match à finaliser
     * @return MatchResultDTO contenant les détails du résultat
     * @throws IllegalArgumentException si le match n'existe pas ou est déjà finalisé
     */
    @Transactional
    public MatchResultDTO finalizeMatch(Integer matchId) {
        GameMatch match = gameMatchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found with ID: " + matchId));

        if (match.getStatus() == null || !match.getStatus()) {
            throw new IllegalArgumentException("Match must have both players to be finalized");
        }

        if (match.getFinalized() != null && match.getFinalized()) {
            throw new IllegalArgumentException("Match is already finalized");
        }

        // Récupérer les informations du match
        User user1 = match.getUser1();
        User user2 = match.getUser2();
        Integer user1CorrectAnswers = calculatePlayerScore(matchId, user1.getId());
        Integer user2CorrectAnswers = calculatePlayerScore(matchId, user2.getId());

        log.info("Match {} finalized: User1({}) scored {}, User2({}) scored {}",
                matchId, user1.getLogin(), user1CorrectAnswers, user2.getLogin(), user2CorrectAnswers);

        // Déterminer le gagnant et attribuer les points
        int user1PointsGained;
        int user2PointsGained;
        Integer winnerId;
        String winnerLogin;

        if (user1CorrectAnswers > user2CorrectAnswers) {
            winnerId = user1.getId();
            winnerLogin = user1.getLogin();
            user1PointsGained = calculatePointsForVictory(user1CorrectAnswers, user2CorrectAnswers);
            user2PointsGained = calculatePointsForDefeat(user1CorrectAnswers, user2CorrectAnswers);
        } else if (user2CorrectAnswers > user1CorrectAnswers) {
            winnerId = user2.getId();
            winnerLogin = user2.getLogin();
            user2PointsGained = calculatePointsForVictory(user2CorrectAnswers, user1CorrectAnswers);
            user1PointsGained = calculatePointsForDefeat(user2CorrectAnswers, user1CorrectAnswers);
        } else {
            // En cas d'égalité
            winnerId = null;
            winnerLogin = "TIE";
            user1PointsGained = calculatePointsForTie();
            user2PointsGained = calculatePointsForTie();
        }

        // Mise à jour des points des utilisateurs
        if (winnerId != null) {
            rankingIntegrationService.recordWin(winnerId, user1PointsGained > user2PointsGained ? user1PointsGained : user2PointsGained);
            Integer loserId = winnerId.equals(user1.getId()) ? user2.getId() : user1.getId();
            rankingIntegrationService.recordLoss(loserId, 0);
        } else {
            // En cas d'égalité
            rankingIntegrationService.recordTie(user1.getId(), user1PointsGained);
            rankingIntegrationService.recordTie(user2.getId(), user2PointsGained);
        }

        // Marquer le match comme finalisé
        match.setFinalized(true);
        gameMatchRepository.save(match);

        return new MatchResultDTO(
                matchId,
                user1.getId(),
                user1.getLogin(),
                user1CorrectAnswers,
                user1PointsGained,
                user2.getId(),
                user2.getLogin(),
                user2CorrectAnswers,
                user2PointsGained,
                winnerId,
                winnerLogin
        );
    }

    /**
     * Calcule le score total d'un joueur pour un match (somme des réponses correctes de tous les rounds).
     *
     * @param matchId L'ID du match
     * @param playerId L'ID du joueur
     * @return Le nombre total de réponses correctes
     */
    private Integer calculatePlayerScore(Integer matchId, Integer playerId) {
        List<RoundAnswer> answers = roundAnswerRepository.findAllPlayerAnswersInMatch(matchId, playerId);
        return answers.stream()
                .mapToInt(answer -> answer.getCorrect_answers() != null ? answer.getCorrect_answers() : 0)
                .sum();
    }

    /**
     * Calcule les points gagnés en cas de victoire.
     * Formule : 100 + (différence de points * 2)
     *
     * @param winnerScore Le score du gagnant
     * @param loserScore Le score du perdant
     * @return Les points gagnés
     */
    private Integer calculatePointsForVictory(Integer winnerScore, Integer loserScore) {
        int basPoints = 100;
        int scoreDifference = Math.max(0, winnerScore - loserScore);
        return basPoints + (scoreDifference * 2);
    }

    /**
     * Calcule les points gagnés en cas de défaite.
     * Formule : 20 + max(0, loserScore - 5)
     *
     * @param winnerScore Le score du gagnant
     * @param loserScore Le score du perdant
     * @return Les points gagnés (consolation)
     */
    private Integer calculatePointsForDefeat(Integer winnerScore, Integer loserScore) {
        int consolePoints = 20;
        return consolePoints + Math.max(0, loserScore - 5);
    }

    /**
     * Calcule les points gagnés en cas d'égalité.
     * Formule : 50 points
     *
     * @return Les points gagnés (égalité)
     */
    private Integer calculatePointsForTie() {
        return 50;
    }

    /**
     * Récupère les statistiques complètes d'un match avant sa finalisation.
     *
     * @param matchId L'ID du match
     * @return Les statistiques du match
     */
    public MatchResultDTO getMatchStats(Integer matchId) {
        GameMatch match = gameMatchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found with ID: " + matchId));

        if (match.getUser2() == null) {
            throw new IllegalArgumentException("Match is not yet full (waiting for second player)");
        }

        User user1 = match.getUser1();
        User user2 = match.getUser2();
        Integer user1Score = calculatePlayerScore(matchId, user1.getId());
        Integer user2Score = calculatePlayerScore(matchId, user2.getId());

        return new MatchResultDTO(
                matchId,
                user1.getId(),
                user1.getLogin(),
                user1Score,
                0, // Points not yet calculated
                user2.getId(),
                user2.getLogin(),
                user2Score,
                0, // Points not yet calculated
                null, // No winner yet
                "ONGOING"
        );
    }

    /**
     * Récupère tous les matchs d'un utilisateur.
     *
     * @param userId L'ID de l'utilisateur
     * @return Liste des matchs de l'utilisateur
     */
    public List<GameMatch> getUserMatches(Integer userId) {
        return gameMatchRepository.findAllMatchesByUserId(userId);
    }

    /**
     * Récupère les matchs terminés d'un utilisateur.
     *
     * @param userId L'ID de l'utilisateur
     * @return Liste des matchs terminés
     */
    public List<GameMatch> getUserCompletedMatches(Integer userId) {
        return gameMatchRepository.findAllMatchesByUserId(userId).stream()
                .filter(m -> m.getStatus() != null && m.getStatus())
                .toList();
    }
}

