package fr.lachaisedusavoir.controller;

import fr.lachaisedusavoir.dto.AuthRequestDto;
import fr.lachaisedusavoir.dto.ErrorResponseDto;
import fr.lachaisedusavoir.models.Session;
import fr.lachaisedusavoir.service.AuthService;
import fr.lachaisedusavoir.dto.AuthResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AuthRequestDto requestDto) {
        try {
            Session session = authService.signup(requestDto.login(), requestDto.password());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponseDto(session.getApiToken()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDto(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Une erreur est survenue lors de l'inscription"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDto requestDto) {
        try {
            Session session = authService.login(requestDto.login(), requestDto.password());
            return ResponseEntity.ok(new AuthResponseDto(session.getApiToken()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDto(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Une erreur est survenue lors de la connexion"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Integer userId) {
            authService.logout(userId);
        }
        return ResponseEntity.noContent().build();
    }
}
