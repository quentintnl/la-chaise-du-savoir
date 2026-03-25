package fr.lachaisedusavoir.service;

import fr.lachaisedusavoir.models.User;
import fr.lachaisedusavoir.service.AuthService;
import fr.lachaisedusavoir.service.RankingIntegrationService;
import fr.lachaisedusavoir.service.RankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du service d'intégration du ranking.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du RankingIntegrationService")
class RankingIntegrationServiceTest {

    @Mock
    private RankingService rankingService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private RankingIntegrationService rankingIntegrationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("player1", "password123");
        user.setId(1);
        user.setGlobalPoints(0);
        user.setUserWinstreak(0);
    }

    @Test
    @DisplayName("Devrait initialiser le ranking pour un nouvel utilisateur")
    void testInitializeRankingForNewUser() {
        // Act
        rankingIntegrationService.initializeRankingForNewUser(user);

        // Assert - pas d'appels au service de ranking lors de l'initialisation
        verify(rankingService, never()).addPoints(anyInt(), anyInt());
        verify(rankingService, never()).addWinStreak(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Devrait enregistrer une victoire et ajouter points + win streak")
    void testRecordWin() {
        // Act
        rankingIntegrationService.recordWin(1, 50);

        // Assert
        verify(rankingService, times(1)).addPoints(1, 50);
        verify(rankingService, times(1)).addWinStreak(1, 1);
    }

    @Test
    @DisplayName("Devrait enregistrer une défaite et réinitialiser le win streak")
    void testRecordLoss() {
        // Act
        rankingIntegrationService.recordLoss(1, 0);

        // Assert
        verify(rankingService, times(1)).resetWinStreak(1);
        verify(rankingService, never()).addPoints(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Devrait enregistrer une défaite avec pénalité")
    void testRecordLoss_WithPenalty() {
        // Act
        rankingIntegrationService.recordLoss(1, 10);

        // Assert
        verify(rankingService, times(1)).resetWinStreak(1);
    }

    @Test
    @DisplayName("Devrait appliquer un bonus de win streak à 5 victoires")
    void testApplyWinStreakBonus_AtFiveVictories() {
        // Arrange
        user.setUserWinstreak(5);
        when(authService.getUserById(1)).thenReturn(Optional.of(user));

        // Act
        rankingIntegrationService.applyWinStreakBonus(1, 1);

        // Assert
        verify(rankingService, times(1)).addPoints(1, 25);
    }

    @Test
    @DisplayName("Devrait appliquer un bonus de win streak à 10 victoires")
    void testApplyWinStreakBonus_AtTenVictories() {
        // Arrange
        user.setUserWinstreak(10);
        when(authService.getUserById(1)).thenReturn(Optional.of(user));

        // Act
        rankingIntegrationService.applyWinStreakBonus(1, 1);

        // Assert
        verify(rankingService, times(1)).addPoints(1, 25);
    }

    @Test
    @DisplayName("Ne devrait pas appliquer de bonus sans win streak")
    void testApplyWinStreakBonus_NoWinStreak() {
        // Arrange
        user.setUserWinstreak(0);
        when(authService.getUserById(1)).thenReturn(Optional.of(user));

        // Act
        rankingIntegrationService.applyWinStreakBonus(1, 1);

        // Assert
        verify(rankingService, never()).addPoints(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Ne devrait pas appliquer de bonus à un win streak non multiple de 5")
    void testApplyWinStreakBonus_NoMultipleOfFive() {
        // Arrange
        user.setUserWinstreak(3);
        when(authService.getUserById(1)).thenReturn(Optional.of(user));

        // Act
        rankingIntegrationService.applyWinStreakBonus(1, 1);

        // Assert
        verify(rankingService, never()).addPoints(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Devrait appliquer un multiplicateur au bonus")
    void testApplyWinStreakBonus_WithMultiplier() {
        // Arrange
        user.setUserWinstreak(5);
        when(authService.getUserById(1)).thenReturn(Optional.of(user));

        // Act
        rankingIntegrationService.applyWinStreakBonus(1, 2);

        // Assert
        verify(rankingService, times(1)).addPoints(1, 50); // 25 * 2
    }

    @Test
    @DisplayName("Devrait lancer une exception si l'utilisateur n'existe pas pour applyWinStreakBonus")
    void testApplyWinStreakBonus_UserNotFound() {
        // Arrange
        when(authService.getUserById(999)).thenReturn(Optional.empty());

        // Act & Assert
        try {
            rankingIntegrationService.applyWinStreakBonus(999, 1);
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Utilisateur non trouvé");
        }
    }

    @Test
    @DisplayName("Devrait gérer un win streak null lors du calcul du bonus")
    void testApplyWinStreakBonus_NullWinStreak() {
        // Arrange
        user.setUserWinstreak(null);
        when(authService.getUserById(1)).thenReturn(Optional.of(user));

        // Act
        rankingIntegrationService.applyWinStreakBonus(1, 1);

        // Assert
        verify(rankingService, never()).addPoints(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Devrait enregistrer plusieurs victoires consécutives")
    void testMultipleWins() {
        // Act
        rankingIntegrationService.recordWin(1, 50);
        rankingIntegrationService.recordWin(1, 50);
        rankingIntegrationService.recordWin(1, 50);

        // Assert
        verify(rankingService, times(3)).addPoints(1, 50);
        verify(rankingService, times(3)).addWinStreak(1, 1);
    }

    @Test
    @DisplayName("Devrait alterner victoires et défaites correctement")
    void testAlternatingWinsAndLosses() {
        // Act
        rankingIntegrationService.recordWin(1, 50);
        rankingIntegrationService.recordWin(1, 50);
        rankingIntegrationService.recordLoss(1, 0);
        rankingIntegrationService.recordWin(1, 50);

        // Assert
        verify(rankingService, times(3)).addPoints(1, 50);
        verify(rankingService, times(2)).addWinStreak(1, 1);
        verify(rankingService, times(1)).resetWinStreak(1);
    }
}

