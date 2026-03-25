package fr.lachaisedusavoir.service;

import fr.lachaisedusavoir.dto.RankingDTO;
import fr.lachaisedusavoir.models.User;
import fr.lachaisedusavoir.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion du système de ranking des utilisateurs.
 * Fournit des fonctionnalités pour calculer et récupérer les classements.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private final UserRepository userRepository;

    /**
     * Récupère le classement global de tous les utilisateurs par points décroissants.
     *
     * @return Liste des utilisateurs classés par points décroissants
     */
    public List<RankingDTO> getGlobalRanking() {
        log.info("Récupération du classement global");
        List<User> users = userRepository.findAll();
        return convertToRankingDTOs(users, true);
    }

    /**
     * Récupère le classement des utilisateurs par win streak décroissant.
     *
     * @return Liste des utilisateurs classés par win streak décroissant
     */
    public List<RankingDTO> getWinStreakRanking() {
        log.info("Récupération du classement par win streak");
        List<User> users = userRepository.findAll();
        return convertToRankingDTOs(users, false);
    }

    /**
     * Récupère le rang d'un utilisateur spécifique dans le classement global.
     *
     * @param userId L'identifiant de l'utilisateur
     * @return Le DTO contenant les informations de rang et points
     * @throws IllegalArgumentException si l'utilisateur n'existe pas
     */
    public RankingDTO getUserRanking(Integer userId) {
        log.info("Récupération du rang de l'utilisateur: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));

        List<RankingDTO> globalRanking = getGlobalRanking();
        for (int i = 0; i < globalRanking.size(); i++) {
            RankingDTO rankingDTO = globalRanking.get(i);
            if (rankingDTO.userId().equals(userId)) {
                return rankingDTO;
            }
        }

        // Si l'utilisateur n'a pas de points, il ne sera pas dans le classement
        return new RankingDTO(
                user.getId(),
                user.getLogin(),
                user.getGlobalPoints() != null ? user.getGlobalPoints() : 0,
                globalRanking.size() + 1
        );
    }

    /**
     * Convertit une liste d'utilisateurs en liste de DTOs de ranking.
     *
     * @param users La liste des utilisateurs
     * @param sortByPoints true pour trier par points, false pour trier par win streak
     * @return Liste des RankingDTOs triés
     */
    private List<RankingDTO> convertToRankingDTOs(List<User> users, boolean sortByPoints) {
        List<RankingDTO> rankings = users.stream()
                .map(user -> new RankingDTO(
                        user.getId(),
                        user.getLogin(),
                        sortByPoints ? (user.getGlobalPoints() != null ? user.getGlobalPoints() : 0)
                                : (user.getUserWinstreak() != null ? user.getUserWinstreak() : 0),
                        0 // Le rang sera défini après le tri
                ))
                .collect(Collectors.toList());

        // Trier par points/win streak en ordre décroissant
        Collections.sort(rankings, (r1, r2) -> Integer.compare(r2.score(), r1.score()));

        // Attribuer les rangs (1-indexed)
        for (int i = 0; i < rankings.size(); i++) {
            rankings.set(i, new RankingDTO(
                    rankings.get(i).userId(),
                    rankings.get(i).userLogin(),
                    rankings.get(i).score(),
                    i + 1
            ));
        }

        return rankings;
    }

    /**
     * Ajoute des points à un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @param points Les points à ajouter
     * @return L'utilisateur mis à jour
     * @throws IllegalArgumentException si l'utilisateur n'existe pas ou si les points sont négatifs
     */
    public User addPoints(Integer userId, Integer points) {
        if (points < 0) {
            throw new IllegalArgumentException("Les points doivent être positifs");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));

        int currentPoints = user.getGlobalPoints() != null ? user.getGlobalPoints() : 0;
        user.setGlobalPoints(currentPoints + points);
        log.info("Points ajoutés pour l'utilisateur {}: +{} (total: {})",
                userId, points, user.getGlobalPoints());

        return userRepository.save(user);
    }

    /**
     * Ajoute au win streak d'un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @param streak Le nombre de victoires à ajouter
     * @return L'utilisateur mis à jour
     * @throws IllegalArgumentException si l'utilisateur n'existe pas ou si streak est négatif
     */
    public User addWinStreak(Integer userId, Integer streak) {
        if (streak < 0) {
            throw new IllegalArgumentException("Le win streak doit être positif");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));

        int currentStreak = user.getUserWinstreak() != null ? user.getUserWinstreak() : 0;
        user.setUserWinstreak(currentStreak + streak);
        log.info("Win streak augmenté pour l'utilisateur {}: +{} (total: {})",
                userId, streak, user.getUserWinstreak());

        return userRepository.save(user);
    }

    /**
     * Réinitialise le win streak d'un utilisateur à 0.
     *
     * @param userId L'identifiant de l'utilisateur
     * @return L'utilisateur mis à jour
     * @throws IllegalArgumentException si l'utilisateur n'existe pas
     */
    public User resetWinStreak(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));

        user.setUserWinstreak(0);
        log.info("Win streak réinitialisé pour l'utilisateur {}", userId);

        return userRepository.save(user);
    }
}

