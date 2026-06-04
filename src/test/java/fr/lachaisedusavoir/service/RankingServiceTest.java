package fr.lachaisedusavoir.service;

import fr.lachaisedusavoir.dto.RankingDTO;
import fr.lachaisedusavoir.models.User;
import fr.lachaisedusavoir.repository.UserRepository;
import fr.lachaisedusavoir.service.RankingService;
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
 * Tests unitaires du service de ranking.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du RankingService")
class RankingServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RankingService rankingService;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = new User("alice", "password123");
        user1.setId(1);
        user1.setGlobalPoints(100);
        user1.setUserWinstreak(5);

        user2 = new User("bob", "password123");
        user2.setId(2);
        user2.setGlobalPoints(200);
        user2.setUserWinstreak(3);

        user3 = new User("charlie", "password123");
        user3.setId(3);
        user3.setGlobalPoints(150);
        user3.setUserWinstreak(7);
    }

    @Test
    @DisplayName("Devrait récupérer le classement global trié par points décroissants")
    void testGetGlobalRanking() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));

        // Act
        List<RankingDTO> rankings = rankingService.getGlobalRanking();

        // Assert
        assertThat(rankings).hasSize(3);
        assertThat(rankings.get(0).userLogin()).isEqualTo("bob");
        assertThat(rankings.get(0).score()).isEqualTo(200);
        assertThat(rankings.get(0).rank()).isEqualTo(1);

        assertThat(rankings.get(1).userLogin()).isEqualTo("charlie");
        assertThat(rankings.get(1).score()).isEqualTo(150);
        assertThat(rankings.get(1).rank()).isEqualTo(2);

        assertThat(rankings.get(2).userLogin()).isEqualTo("alice");
        assertThat(rankings.get(2).score()).isEqualTo(100);
        assertThat(rankings.get(2).rank()).isEqualTo(3);

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Devrait récupérer le classement par win streak trié décroissant")
    void testGetWinStreakRanking() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));

        // Act
        List<RankingDTO> rankings = rankingService.getWinStreakRanking();

        // Assert
        assertThat(rankings).hasSize(3);
        assertThat(rankings.get(0).userLogin()).isEqualTo("charlie");
        assertThat(rankings.get(0).score()).isEqualTo(7);
        assertThat(rankings.get(0).rank()).isEqualTo(1);

        assertThat(rankings.get(1).userLogin()).isEqualTo("alice");
        assertThat(rankings.get(1).score()).isEqualTo(5);
        assertThat(rankings.get(1).rank()).isEqualTo(2);

        assertThat(rankings.get(2).userLogin()).isEqualTo("bob");
        assertThat(rankings.get(2).score()).isEqualTo(3);
        assertThat(rankings.get(2).rank()).isEqualTo(3);

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Devrait récupérer le rang d'un utilisateur existant")
    void testGetUserRanking_UserExists() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));
        when(userRepository.findById(2)).thenReturn(Optional.of(user2));

        // Act
        RankingDTO ranking = rankingService.getUserRanking(2);

        // Assert
        assertThat(ranking.userId()).isEqualTo(2);
        assertThat(ranking.userLogin()).isEqualTo("bob");
        assertThat(ranking.score()).isEqualTo(200);
        assertThat(ranking.rank()).isEqualTo(1);

        verify(userRepository, times(1)).findById(2);
    }

    @Test
    @DisplayName("Devrait lancer une exception quand l'utilisateur n'existe pas pour getUserRanking")
    void testGetUserRanking_UserNotFound() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> rankingService.getUserRanking(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");
    }

    @Test
    @DisplayName("Devrait ajouter des points à un utilisateur")
    void testAddPoints() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // Act
        User result = rankingService.addPoints(1, 50);

        // Assert
        assertThat(result.getGlobalPoints()).isEqualTo(150);
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Devrait ajouter 0 point sans erreur")
    void testAddPoints_ZeroPoints() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // Act
        User result = rankingService.addPoints(1, 0);

        // Assert
        assertThat(result.getGlobalPoints()).isEqualTo(100);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Devrait lancer une exception quand on ajoute des points négatifs")
    void testAddPoints_NegativePoints() {
        // Act & Assert
        assertThatThrownBy(() -> rankingService.addPoints(1, -10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Les points doivent être positifs");
    }

    @Test
    @DisplayName("Devrait lancer une exception quand l'utilisateur n'existe pas pour addPoints")
    void testAddPoints_UserNotFound() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> rankingService.addPoints(999, 50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");
    }

    @Test
    @DisplayName("Devrait ajouter du win streak à un utilisateur")
    void testAddWinStreak() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // Act
        User result = rankingService.addWinStreak(1, 3);

        // Assert
        assertThat(result.getUserWinstreak()).isEqualTo(8);
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Devrait ajouter 0 win streak sans erreur")
    void testAddWinStreak_ZeroStreak() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // Act
        User result = rankingService.addWinStreak(1, 0);

        // Assert
        assertThat(result.getUserWinstreak()).isEqualTo(5);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Devrait lancer une exception quand on ajoute un win streak négatif")
    void testAddWinStreak_NegativeStreak() {
        // Act & Assert
        assertThatThrownBy(() -> rankingService.addWinStreak(1, -5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Le win streak doit être positif");
    }

    @Test
    @DisplayName("Devrait lancer une exception quand l'utilisateur n'existe pas pour addWinStreak")
    void testAddWinStreak_UserNotFound() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> rankingService.addWinStreak(999, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");
    }

    @Test
    @DisplayName("Devrait réinitialiser le win streak d'un utilisateur")
    void testResetWinStreak() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // Act
        User result = rankingService.resetWinStreak(1);

        // Assert
        assertThat(result.getUserWinstreak()).isEqualTo(0);
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Devrait lancer une exception quand l'utilisateur n'existe pas pour resetWinStreak")
    void testResetWinStreak_UserNotFound() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> rankingService.resetWinStreak(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");
    }

    @Test
    @DisplayName("Devrait gérer les valeurs null des points globaux")
    void testGetGlobalRanking_WithNullGlobalPoints() {
        // Arrange
        user1.setGlobalPoints(null);
        user2.setGlobalPoints(null);
        user3.setGlobalPoints(null);

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));

        // Act
        List<RankingDTO> rankings = rankingService.getGlobalRanking();

        // Assert
        assertThat(rankings).hasSize(3);
        assertThat(rankings).allMatch(r -> r.score() == 0);
    }

    @Test
    @DisplayName("Devrait gérer les valeurs null du win streak")
    void testGetWinStreakRanking_WithNullWinStreak() {
        // Arrange
        user1.setUserWinstreak(null);
        user2.setUserWinstreak(null);
        user3.setUserWinstreak(null);

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));

        // Act
        List<RankingDTO> rankings = rankingService.getWinStreakRanking();

        // Assert
        assertThat(rankings).hasSize(3);
        assertThat(rankings).allMatch(r -> r.score() == 0);
    }

    @Test
    @DisplayName("Devrait gérer un utilisateur avec globalPoints null dans addPoints")
    void testAddPoints_WithNullGlobalPoints() {
        // Arrange
        user1.setGlobalPoints(null);
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // Act
        User result = rankingService.addPoints(1, 50);

        // Assert
        assertThat(result.getGlobalPoints()).isEqualTo(50);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Devrait gérer un utilisateur avec userWinstreak null dans addWinStreak")
    void testAddWinStreak_WithNullWinStreak() {
        // Arrange
        user1.setUserWinstreak(null);
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // Act
        User result = rankingService.addWinStreak(1, 3);

        // Assert
        assertThat(result.getUserWinstreak()).isEqualTo(3);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Devrait retourner une liste vide quand il n'y a pas d'utilisateurs")
    void testGetGlobalRanking_EmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<RankingDTO> rankings = rankingService.getGlobalRanking();

        // Assert
        assertThat(rankings).isEmpty();
    }
}

