package fr.lachaisedusavoir.controller;

import fr.lachaisedusavoir.models.GameMatch;
import fr.lachaisedusavoir.service.MatchService;
import fr.lachaisedusavoir.dto.MatchResponseDto;
import fr.lachaisedusavoir.models.User;
import fr.lachaisedusavoir.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
@Slf4j
public class MatchController {

    private final MatchService matchService;
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
        return ResponseEntity.ok(matchService.getQuestion());
    }

    @PostMapping("/{matchId}/answer")
    public ResponseEntity<?> submitAnswer(@PathVariable Integer matchId, @RequestParam boolean isCorrect, Authentication authentication) {
        try {
            String login = (String) authentication.getPrincipal();
            User user = userRepository.findByLogin(login).orElseThrow(() -> new RuntimeException("User not found"));
            String result = matchService.submitAnswer(matchId, user.getId(), isCorrect);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Unexpected error during answer submission", e);
            return ResponseEntity.status(500).body("Une erreur est survenue: " + e.getMessage());
        }
    }
}
