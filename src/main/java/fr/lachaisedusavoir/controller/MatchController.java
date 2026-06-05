package fr.lachaisedusavoir.controller;

import fr.lachaisedusavoir.dto.QuestionDto;
import fr.lachaisedusavoir.models.GameMatch;
import fr.lachaisedusavoir.service.JsonToQuestionDtoParserService;
import fr.lachaisedusavoir.service.MatchService;
import fr.lachaisedusavoir.service.MatchResultService;
import fr.lachaisedusavoir.dto.MatchResponseDto;
import fr.lachaisedusavoir.dto.MatchResultDTO;
import fr.lachaisedusavoir.models.User;
import fr.lachaisedusavoir.repository.UserRepository;
import fr.lachaisedusavoir.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
@Slf4j
public class MatchController {

    private final MatchService matchService;
    private final MatchResultService matchResultService;
    private final QuestionService questionService;
    private final JsonToQuestionDtoParserService jsonToQuestionDtoParserService;
    private final UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createMatch(Authentication authentication) {
        try {
            String login = (String) authentication.getPrincipal();
            User user = userRepository.findByLogin(login).orElseThrow(() -> new RuntimeException("User not found"));
            Integer userId = user.getId();
            GameMatch match = matchService.createMatch(userId);
            
            MatchResponseDto response = new MatchResponseDto(
                    match.getId(),
                    match.getInviteCode(),
                    match.getStatus(),
                    "Partie créée avec succès. Partagez le code d'invitation."
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Unexpected error during match creation", e);
            return ResponseEntity.status(500).body("Une erreur est survenue lors de la création du match");
        }
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinMatch(@RequestParam String inviteCode, Authentication authentication) {
        try {
            String login = (String) authentication.getPrincipal();
            User user = userRepository.findByLogin(login).orElseThrow(() -> new RuntimeException("User not found"));
            Integer userId = user.getId();
            GameMatch match = matchService.joinMatch(inviteCode, userId);
            
            MatchResponseDto response = new MatchResponseDto(
                    match.getId(),
                    match.getInviteCode(),
                    match.getStatus(),
                    "Vous avez rejoint la partie avec succès."
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during match joining", e);
            return ResponseEntity.status(500).body("Une erreur est survenue lors de l'arrivée dans le match");
        }
    }

    @GetMapping("/{matchId}/question")
    public ResponseEntity<?> getQuestion(@PathVariable Integer matchId) {
        try {
            String questionsJson = questionService.fetchQuestions();
            if (questionsJson != null) {
                List<QuestionDto> questions = jsonToQuestionDtoParserService.parse(questionsJson);
                return ResponseEntity.ok(questions);
            }
            return ResponseEntity.status(500).build();
        } catch (Exception e) {
            log.error("Unexpected error during fetching question", e);
            return ResponseEntity.status(500).body("Une erreur est survenue lors de la récupération de la question");
        }
    }

    @PostMapping("/{matchId}/winner")
    public ResponseEntity<?> submitWinner(@PathVariable Integer matchId, Authentication authentication) {
        String login = (String) authentication.getPrincipal();
        User user = userRepository.findByLogin(login).orElseThrow(() -> new RuntimeException("User not found"));

        user.setGlobalPoints(user.getGlobalPoints() + 1);
        userRepository.save(user);
        return ResponseEntity.ok("Match " + matchId + " won by user " + user.getLogin() + " (total points: " + user.getGlobalPoints() + ")");
    }

    /**
     * Finalise un match et retourne les résultats avec gagnant/perdant
     * @param matchId L'ID du match à finaliser
     * @return Les résultats du match (scores, points gagnés, gagnant)
     */
    @PostMapping("/{matchId}/finalize")
    public ResponseEntity<?> finalizeMatch(@PathVariable Integer matchId) {
        try {
            MatchResultDTO result = matchResultService.finalizeMatch(matchId);
            
            log.info("Match #{} finalized - Winner: {} ({} points)", 
                    matchId, result.winnerLogin(), result.user1PointsGained());
            
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finalizing match {}", matchId, e);
            return ResponseEntity.status(500).body("Une erreur est survenue lors de la finalisation du match");
        }
    }

    /**
     * Récupère les statistiques d'un match en cours
     * @param matchId L'ID du match
     * @return Les statistiques du match (scores actuels sans points finalisés)
     */
    @GetMapping("/{matchId}/stats")
    public ResponseEntity<?> getMatchStats(@PathVariable Integer matchId) {
        try {
            MatchResultDTO stats = matchResultService.getMatchStats(matchId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error getting match stats for {}", matchId, e);
            return ResponseEntity.status(500).body("Une erreur est survenue lors de la récupération des statistiques");
        }
    }

    /**
     * Récupère tous les matchs d'un utilisateur
     * @param authentication L'authentification de l'utilisateur
     * @return Liste des matchs de l'utilisateur
     */
    @GetMapping("/user/all")
    public ResponseEntity<?> getUserMatches(Authentication authentication) {
        try {
            String login = (String) authentication.getPrincipal();
            User user = userRepository.findByLogin(login).orElseThrow(() -> new RuntimeException("User not found"));
            
            List<GameMatch> matches = matchResultService.getUserMatches(user.getId());
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            log.error("Error getting user matches", e);
            return ResponseEntity.status(500).body("Une erreur est survenue lors de la récupération des matchs");
        }
    }

    /**
     * Récupère tous les matchs terminés d'un utilisateur
     * @param authentication L'authentification de l'utilisateur
     * @return Liste des matchs terminés de l'utilisateur
     */
    @GetMapping("/user/completed")
    public ResponseEntity<?> getUserCompletedMatches(Authentication authentication) {
        try {
            String login = (String) authentication.getPrincipal();
            User user = userRepository.findByLogin(login).orElseThrow(() -> new RuntimeException("User not found"));
            
            List<GameMatch> matches = matchResultService.getUserCompletedMatches(user.getId());
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            log.error("Error getting user completed matches", e);
            return ResponseEntity.status(500).body("Une erreur est survenue lors de la récupération des matchs terminés");
        }
    }
}
