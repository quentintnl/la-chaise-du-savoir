package fr.lachaisedusavoir.controller;

import fr.lachaisedusavoir.dto.AuthRequestDto;
import fr.lachaisedusavoir.dto.ErrorResponseDto;
import fr.lachaisedusavoir.models.Session;
import fr.lachaisedusavoir.service.AuthService;
import fr.lachaisedusavoir.dto.AuthResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AuthRequestDto requestDto) {
        try {
            log.info("Signup endpoint called with login: {}", requestDto.login());
            Session session = authService.signup(requestDto.login(), requestDto.password());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponseDto(session.getApiToken()));
        } catch (IllegalArgumentException e) {
            log.error("Signup error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDto(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during signup", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Une erreur est survenue lors de l'inscription"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDto requestDto) {
        try {
            log.info("Login endpoint called with login: {}", requestDto.login());
            Session session = authService.login(requestDto.login(), requestDto.password());
            return ResponseEntity.ok(new AuthResponseDto(session.getApiToken()));
        } catch (IllegalArgumentException e) {
            log.error("Login error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDto(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Une erreur est survenue lors de la connexion"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        // Extract userId from token - for now, this is simplified
        // In a real application, you would validate the token and extract the userId
        return ResponseEntity.noContent().build();
    }
}
