package fr.lachaisedusavoir.controller;

import fr.lachaisedusavoir.models.GameMatch;
import fr.lachaisedusavoir.service.MatchService;
import fr.lachaisedusavoir.dto.MatchResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/match")
@RequiredArgsConstructor
@Slf4j
public class MatchController {

    private final MatchService matchService;

    @PostMapping("/create")
    public ResponseEntity<?> createMatch(Authentication authentication) {
        try {
            Integer userId = (Integer) authentication.getPrincipal();
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
            Integer userId = (Integer) authentication.getPrincipal();
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
}
