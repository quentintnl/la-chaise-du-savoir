package fr.lachaisedusavoir.service;

import fr.lachaisedusavoir.dto.MatchResultDTO;
import fr.lachaisedusavoir.models.*;
import fr.lachaisedusavoir.repository.GameMatchRepository;
import fr.lachaisedusavoir.repository.RoundAnswerRepository;
import fr.lachaisedusavoir.repository.RoundsRepository;
import fr.lachaisedusavoir.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du service de résultats de match.
 * Teste l'intégration entre le matching et le ranking.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du MatchResultService")
class MatchResultServiceTest {

    @Mock
    private GameMatchRepository gameMatchRepository;

    @Mock
    private RoundsRepository roundsRepository;

    @Mock
    private RoundAnswerRepository roundAnswerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RankingIntegrationService rankingIntegrationService;

    @InjectMocks
    private MatchResultService matchResultService;

    private User user1;
    private User user2;
    private GameMatch match;
    private Rounds round1;
    private RoundAnswer answer1;
    private RoundAnswer answer2;

    @BeforeEach
    void setUp() {
        // Setup des utilisateurs
        user1 = new User("alice", "password123");
        user1.setId(1);
        user1.setGlobalPoints(100);
        user1.setUserWinstreak(2);

        user2 = new User("bob", "password123");
        user2.setId(2);
        user2.setGlobalPoints(150);
        user2.setUserWinstreak(1);

        // Setup du match
        match = new GameMatch();
        match.setId(1);
        match.setUser1(user1);
        match.setUser2(user2);
        match.setStatus(true);
        match.setFinalized(false);

        // Setup des rounds et réponses
        round1 = new Rounds();
        round1.setId(1);
        round1.setMatch(match);
        round1.setRound_number(1);

        // User1 a 8 bonnes réponses
        answer1 = new RoundAnswer();
        answer1.setId(1);
        answer1.setRound_id(round1);
        answer1.setPlayer_id(1);
        answer1.setCorrect_answers(8);

        // User2 a 6 bonnes réponses
        answer2 = new RoundAnswer();
        answer2.setId(2);
        answer2.setRound_id(round1);
        answer2.setPlayer_id(2);
        answer2.setCorrect_answers(6);
    }

    @Test
    @DisplayName("Devrait finaliser un match avec victoire et enregistrer les points")
    void testFinalizeMatch_WithWinner() {
        // Arrange
        when(gameMatchRepository.findById(1)).thenReturn(Optional.of(match));
        when(roundAnswerRepository.findAllPlayerAnswersInMatch(1, 1)).thenReturn(List.of(answer1));
        when(roundAnswerRepository.findAllPlayerAnswersInMatch(1, 2)).thenReturn(List.of(answer2));

        // Act
        MatchResultDTO result = matchResultService.finalizeMatch(1);

        // Assert
        assertThat(result.matchId()).isEqualTo(1);
        assertThat(result.user1Login()).isEqualTo("alice");
        assertThat(result.user2Login()).isEqualTo("bob");
        assertThat(result.user1Score()).isEqualTo(8);
        assertThat(result.user2Score()).isEqualTo(6);
        assertThat(result.winnerId()).isEqualTo(1);
        assertThat(result.winnerLogin()).isEqualTo("alice");

        // Vérifier que les points ont été enregistrés
        verify(rankingIntegrationService, times(1)).recordWin(1, result.user1PointsGained());
        verify(rankingIntegrationService, times(1)).recordLoss(2, 0);
        verify(gameMatchRepository, times(1)).save(match);
        assertThat(match.getFinalized()).isTrue();
    }

    @Test
    @DisplayName("Devrait finaliser un match avec défaite et enregistrer les points")
    void testFinalizeMatch_WithLoser() {
        // Arrange - User2 a plus de bonnes réponses
        RoundAnswer answer2Updated = new RoundAnswer();
        answer2Updated.setId(2);
        answer2Updated.setRound_id(round1);
        answer2Updated.setPlayer_id(2);
        answer2Updated.setCorrect_answers(10);

        when(gameMatchRepository.findById(1)).thenReturn(Optional.of(match));
        when(roundAnswerRepository.findAllPlayerAnswersInMatch(1, 1)).thenReturn(List.of(answer1));
        when(roundAnswerRepository.findAllPlayerAnswersInMatch(1, 2)).thenReturn(List.of(answer2Updated));

        // Act
        MatchResultDTO result = matchResultService.finalizeMatch(1);

        // Assert
        assertThat(result.winnerId()).isEqualTo(2);
        assertThat(result.winnerLogin()).isEqualTo("bob");
        assertThat(result.user1Score()).isEqualTo(8);
        assertThat(result.user2Score()).isEqualTo(10);

        // Vérifier que les points ont été enregistrés
        verify(rankingIntegrationService, times(1)).recordWin(2, result.user2PointsGained());
        verify(rankingIntegrationService, times(1)).recordLoss(1, 0);
    }

    @Test
    @DisplayName("Devrait finaliser un match avec égalité")
    void testFinalizeMatch_WithTie() {
        // Arrange - Même score
        answer1.setCorrect_answers(8);
        answer2.setCorrect_answers(8);

        when(gameMatchRepository.findById(1)).thenReturn(Optional.of(match));
        when(roundAnswerRepository.findAllPlayerAnswersInMatch(1, 1)).thenReturn(List.of(answer1));
        when(roundAnswerRepository.findAllPlayerAnswersInMatch(1, 2)).thenReturn(List.of(answer2));

        // Act
        MatchResultDTO result = matchResultService.finalizeMatch(1);

        // Assert
        assertThat(result.winnerId()).isNull();
        assertThat(result.winnerLogin()).isEqualTo("TIE");
        assertThat(result.user1PointsGained()).isEqualTo(50);
        assertThat(result.user2PointsGained()).isEqualTo(50);

        // Vérifier que les points d'égalité ont été enregistrés
        verify(rankingIntegrationService, times(1)).recordTie(1, 50);
        verify(rankingIntegrationService, times(1)).recordTie(2, 50);
    }

    @Test
    @DisplayName("Devrait lancer une exception si le match n'existe pas")
    void testFinalizeMatch_MatchNotFound() {
        // Arrange
        when(gameMatchRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> matchResultService.finalizeMatch(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Match not found with ID: 999");
    }

    @Test
    @DisplayName("Devrait lancer une exception si le match n'a pas les deux joueurs")
    void testFinalizeMatch_MatchIncomplete() {
        // Arrange
        match.setStatus(false);
        when(gameMatchRepository.findById(1)).thenReturn(Optional.of(match));

        // Act & Assert
        assertThatThrownBy(() -> matchResultService.finalizeMatch(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Match must have both players to be finalized");
    }

    @Test
    @DisplayName("Devrait lancer une exception si le match est déjà finalisé")
    void testFinalizeMatch_AlreadyFinalized() {
        // Arrange
        match.setFinalized(true);
        when(gameMatchRepository.findById(1)).thenReturn(Optional.of(match));

        // Act & Assert
        assertThatThrownBy(() -> matchResultService.finalizeMatch(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Match is already finalized");
    }

    @Test
    @DisplayName("Devrait récupérer les statistiques d'un match en cours")
    void testGetMatchStats() {
        // Arrange
        when(gameMatchRepository.findById(1)).thenReturn(Optional.of(match));
        when(roundAnswerRepository.findAllPlayerAnswersInMatch(1, 1)).thenReturn(List.of(answer1));
        when(roundAnswerRepository.findAllPlayerAnswersInMatch(1, 2)).thenReturn(List.of(answer2));

        // Act
        MatchResultDTO stats = matchResultService.getMatchStats(1);

        // Assert
        assertThat(stats.matchId()).isEqualTo(1);
        assertThat(stats.user1Score()).isEqualTo(8);
        assertThat(stats.user2Score()).isEqualTo(6);
        assertThat(stats.user1PointsGained()).isEqualTo(0); // Pas encore finalisé
        assertThat(stats.winnerLogin()).isEqualTo("ONGOING");
    }

    @Test
    @DisplayName("Devrait lancer une exception si le match n'a pas les deux joueurs pour les stats")
    void testGetMatchStats_MatchIncomplete() {
        // Arrange
        match.setUser2(null);
        when(gameMatchRepository.findById(1)).thenReturn(Optional.of(match));

        // Act & Assert
        assertThatThrownBy(() -> matchResultService.getMatchStats(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Match is not yet full");
    }

    @Test
    @DisplayName("Devrait récupérer tous les matchs d'un utilisateur")
    void testGetUserMatches() {
        // Arrange
        List<GameMatch> matches = Arrays.asList(match);
        when(gameMatchRepository.findAllMatchesByUserId(1)).thenReturn(matches);

        // Act
        List<GameMatch> result = matchResultService.getUserMatches(1);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
        verify(gameMatchRepository, times(1)).findAllMatchesByUserId(1);
    }

    @Test
    @DisplayName("Devrait récupérer les matchs terminés d'un utilisateur")
    void testGetUserCompletedMatches() {
        // Arrange
        GameMatch match1 = new GameMatch();
        match1.setId(1);
        match1.setStatus(true);

        GameMatch match2 = new GameMatch();
        match2.setId(2);
        match2.setStatus(false);

        List<GameMatch> allMatches = Arrays.asList(match1, match2);
        when(gameMatchRepository.findAllMatchesByUserId(1)).thenReturn(allMatches);

        // Act
        List<GameMatch> result = matchResultService.getUserCompletedMatches(1);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Devrait calculer correctement les points de victoire")
    void testPointCalculation_Victory() {
        // Arrange - User1: 10 points, User2: 5 points
        answer1.setCorrect_answers(10);
        answer2.setCorrect_answers(5);

        when(gameMatchRepository.findById(1)).thenReturn(Optional.of(match));
        when(roundAnswerRepository.findAllPlayerAnswersInMatch(1, 1)).thenReturn(List.of(answer1));
        when(roundAnswerRepository.findAllPlayerAnswersInMatch(1, 2)).thenReturn(List.of(answer2));

        // Act
        MatchResultDTO result = matchResultService.finalizeMatch(1);

        // Assert
        // Points victoire = 100 + (10-5)*2 = 100 + 10 = 110
        assertThat(result.user1PointsGained()).isEqualTo(110);
        // Points défaite = 20 + max(0, 5-5) = 20
        assertThat(result.user2PointsGained()).isEqualTo(20);
    }

    @Test
    @DisplayName("Devrait calculer correctement les points de défaite avec bonus")
    void testPointCalculation_DefeatWithBonus() {
        // Arrange - User1: 5 points, User2: 12 points
        answer1.setCorrect_answers(5);
        answer2.setCorrect_answers(12);

        when(gameMatchRepository.findById(1)).thenReturn(Optional.of(match));
        when(roundAnswerRepository.findAllPlayerAnswersInMatch(1, 1)).thenReturn(List.of(answer1));
        when(roundAnswerRepository.findAllPlayerAnswersInMatch(1, 2)).thenReturn(List.of(answer2));

        // Act
        MatchResultDTO result = matchResultService.finalizeMatch(1);

        // Assert
        // Points victoire = 100 + (12-5)*2 = 100 + 14 = 114
        assertThat(result.user2PointsGained()).isEqualTo(114);
        // Points défaite = 20 + max(0, 5-5) = 20
        assertThat(result.user1PointsGained()).isEqualTo(20);
    }

    @Test
    @DisplayName("Devrait gérer les réponses avec correct_answers null")
    void testFinalizeMatch_WithNullCorrectAnswers() {
        // Arrange
        RoundAnswer answerNull = new RoundAnswer();
        answerNull.setId(1);
        answerNull.setRound_id(round1);
        answerNull.setPlayer_id(1);
        answerNull.setCorrect_answers(null);

        when(gameMatchRepository.findById(1)).thenReturn(Optional.of(match));
        when(roundAnswerRepository.findAllPlayerAnswersInMatch(1, 1)).thenReturn(List.of(answerNull));
        when(roundAnswerRepository.findAllPlayerAnswersInMatch(1, 2)).thenReturn(List.of(answer2));

        // Act
        MatchResultDTO result = matchResultService.finalizeMatch(1);

        // Assert - Score de User1 devrait être 0
        assertThat(result.user1Score()).isEqualTo(0);
        assertThat(result.user2Score()).isEqualTo(6);
    }

    @Test
    @DisplayName("Devrait marquer le match comme finalisé après finalizeMatch")
    void testFinalizeMatch_MarkAsFinalized() {
        // Arrange
        when(gameMatchRepository.findById(1)).thenReturn(Optional.of(match));
        when(roundAnswerRepository.findAllPlayerAnswersInMatch(1, 1)).thenReturn(List.of(answer1));
        when(roundAnswerRepository.findAllPlayerAnswersInMatch(1, 2)).thenReturn(List.of(answer2));

        // Act
        matchResultService.finalizeMatch(1);

        // Assert
        assertThat(match.getFinalized()).isTrue();
        verify(gameMatchRepository, times(1)).save(match);
    }
}
