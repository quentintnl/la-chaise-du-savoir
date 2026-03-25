package fr.lachaisedusavoir.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.lachaisedusavoir.controller.RankingController;
import fr.lachaisedusavoir.dto.RankingDTO;
import fr.lachaisedusavoir.models.User;
import fr.lachaisedusavoir.service.RankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration du contrôleur de ranking.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du RankingController")
class RankingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RankingService rankingService;

    private RankingDTO rankingDTO1;
    private RankingDTO rankingDTO2;
    private RankingDTO rankingDTO3;
    private User user1;

    @BeforeEach
    void setUp() {
        RankingController controller = new RankingController(rankingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        rankingDTO1 = new RankingDTO(1, "alice", 100, 1);
        rankingDTO2 = new RankingDTO(2, "bob", 200, 2);
        rankingDTO3 = new RankingDTO(3, "charlie", 150, 3);

        user1 = new User("alice", "password123");
        user1.setId(1);
        user1.setGlobalPoints(100);
        user1.setUserWinstreak(5);
    }

    @Test
    @DisplayName("Devrait récupérer le classement global avec GET /api/ranking/global")
    void testGetGlobalRanking() throws Exception {
        // Arrange
        List<RankingDTO> rankings = Arrays.asList(rankingDTO2, rankingDTO3, rankingDTO1);
        when(rankingService.getGlobalRanking()).thenReturn(rankings);

        // Act & Assert
        mockMvc.perform(get("/api/ranking/global")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].userLogin", equalTo("bob")))
                .andExpect(jsonPath("$[0].score", equalTo(200)))
                .andExpect(jsonPath("$[1].userLogin", equalTo("charlie")))
                .andExpect(jsonPath("$[2].userLogin", equalTo("alice")));

        verify(rankingService, times(1)).getGlobalRanking();
    }

    @Test
    @DisplayName("Devrait récupérer le classement par win streak avec GET /api/ranking/winstreak")
    void testGetWinStreakRanking() throws Exception {
        // Arrange
        List<RankingDTO> rankings = Arrays.asList(rankingDTO1, rankingDTO3, rankingDTO2);
        when(rankingService.getWinStreakRanking()).thenReturn(rankings);

        // Act & Assert
        mockMvc.perform(get("/api/ranking/winstreak")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].userLogin", equalTo("alice")))
                .andExpect(jsonPath("$[1].userLogin", equalTo("charlie")))
                .andExpect(jsonPath("$[2].userLogin", equalTo("bob")));

        verify(rankingService, times(1)).getWinStreakRanking();
    }

    @Test
    @DisplayName("Devrait récupérer le rang d'un utilisateur avec GET /api/ranking/user/{userId}")
    void testGetUserRanking() throws Exception {
        // Arrange
        when(rankingService.getUserRanking(1)).thenReturn(rankingDTO1);

        // Act & Assert
        mockMvc.perform(get("/api/ranking/user/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", equalTo(1)))
                .andExpect(jsonPath("$.userLogin", equalTo("alice")))
                .andExpect(jsonPath("$.score", equalTo(100)))
                .andExpect(jsonPath("$.rank", equalTo(1)));

        verify(rankingService, times(1)).getUserRanking(1);
    }

    @Test
    @DisplayName("Devrait retourner 404 quand l'utilisateur n'existe pas pour getUserRanking")
    void testGetUserRanking_UserNotFound() throws Exception {
        // Arrange
        when(rankingService.getUserRanking(999))
                .thenThrow(new IllegalArgumentException("Utilisateur non trouvé avec l'ID: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/ranking/user/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(rankingService, times(1)).getUserRanking(999);
    }

    @Test
    @DisplayName("Devrait ajouter des points avec POST /api/ranking/user/{userId}/add-points")
    void testAddPoints() throws Exception {
        // Arrange
        user1.setGlobalPoints(150);
        when(rankingService.addPoints(1, 50)).thenReturn(user1);

        // Act & Assert
        mockMvc.perform(post("/api/ranking/user/1/add-points")
                        .param("points", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.login", equalTo("alice")))
                .andExpect(jsonPath("$.globalPoints", equalTo(150)));

        verify(rankingService, times(1)).addPoints(1, 50);
    }

    @Test
    @DisplayName("Devrait retourner 400 quand on ajoute des points négatifs")
    void testAddPoints_NegativePoints() throws Exception {
        // Arrange
        when(rankingService.addPoints(1, -10))
                .thenThrow(new IllegalArgumentException("Les points doivent être positifs"));

        // Act & Assert
        mockMvc.perform(post("/api/ranking/user/1/add-points")
                        .param("points", "-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(rankingService, times(1)).addPoints(1, -10);
    }

    @Test
    @DisplayName("Devrait retourner 400 quand l'utilisateur n'existe pas pour addPoints")
    void testAddPoints_UserNotFound() throws Exception {
        // Arrange
        when(rankingService.addPoints(999, 50))
                .thenThrow(new IllegalArgumentException("Utilisateur non trouvé avec l'ID: 999"));

        // Act & Assert
        mockMvc.perform(post("/api/ranking/user/999/add-points")
                        .param("points", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(rankingService, times(1)).addPoints(999, 50);
    }

    @Test
    @DisplayName("Devrait ajouter du win streak avec POST /api/ranking/user/{userId}/add-winstreak")
    void testAddWinStreak() throws Exception {
        // Arrange
        user1.setUserWinstreak(8);
        when(rankingService.addWinStreak(1, 3)).thenReturn(user1);

        // Act & Assert
        mockMvc.perform(post("/api/ranking/user/1/add-winstreak")
                        .param("streak", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.login", equalTo("alice")))
                .andExpect(jsonPath("$.userWinstreak", equalTo(8)));

        verify(rankingService, times(1)).addWinStreak(1, 3);
    }

    @Test
    @DisplayName("Devrait retourner 400 quand on ajoute un win streak négatif")
    void testAddWinStreak_NegativeStreak() throws Exception {
        // Arrange
        when(rankingService.addWinStreak(1, -5))
                .thenThrow(new IllegalArgumentException("Le win streak doit être positif"));

        // Act & Assert
        mockMvc.perform(post("/api/ranking/user/1/add-winstreak")
                        .param("streak", "-5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(rankingService, times(1)).addWinStreak(1, -5);
    }

    @Test
    @DisplayName("Devrait retourner 400 quand l'utilisateur n'existe pas pour addWinStreak")
    void testAddWinStreak_UserNotFound() throws Exception {
        // Arrange
        when(rankingService.addWinStreak(999, 3))
                .thenThrow(new IllegalArgumentException("Utilisateur non trouvé avec l'ID: 999"));

        // Act & Assert
        mockMvc.perform(post("/api/ranking/user/999/add-winstreak")
                        .param("streak", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(rankingService, times(1)).addWinStreak(999, 3);
    }

    @Test
    @DisplayName("Devrait réinitialiser le win streak avec POST /api/ranking/user/{userId}/reset-winstreak")
    void testResetWinStreak() throws Exception {
        // Arrange
        user1.setUserWinstreak(0);
        when(rankingService.resetWinStreak(1)).thenReturn(user1);

        // Act & Assert
        mockMvc.perform(post("/api/ranking/user/1/reset-winstreak")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.login", equalTo("alice")))
                .andExpect(jsonPath("$.userWinstreak", equalTo(0)));

        verify(rankingService, times(1)).resetWinStreak(1);
    }

    @Test
    @DisplayName("Devrait retourner 404 quand l'utilisateur n'existe pas pour resetWinStreak")
    void testResetWinStreak_UserNotFound() throws Exception {
        // Arrange
        when(rankingService.resetWinStreak(999))
                .thenThrow(new IllegalArgumentException("Utilisateur non trouvé avec l'ID: 999"));

        // Act & Assert
        mockMvc.perform(post("/api/ranking/user/999/reset-winstreak")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(rankingService, times(1)).resetWinStreak(999);
    }

    @Test
    @DisplayName("Devrait ajouter 0 point sans erreur")
    void testAddPoints_ZeroPoints() throws Exception {
        // Arrange
        when(rankingService.addPoints(1, 0)).thenReturn(user1);

        // Act & Assert
        mockMvc.perform(post("/api/ranking/user/1/add-points")
                        .param("points", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(rankingService, times(1)).addPoints(1, 0);
    }

    @Test
    @DisplayName("Devrait ajouter 0 win streak sans erreur")
    void testAddWinStreak_ZeroStreak() throws Exception {
        // Arrange
        when(rankingService.addWinStreak(1, 0)).thenReturn(user1);

        // Act & Assert
        mockMvc.perform(post("/api/ranking/user/1/add-winstreak")
                        .param("streak", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(rankingService, times(1)).addWinStreak(1, 0);
    }
}

