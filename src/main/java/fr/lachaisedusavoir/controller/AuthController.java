package fr.lachaisedusavoir.controller;

import fr.lachaisedusavoir.model.Session;
import fr.lachaisedusavoir.service.AuthService;
import fr.lachaisedusavoir.dto.AuthRequestDto;
import fr.lachaisedusavoir.dto.AuthResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDto> signup(@Valid @RequestBody AuthRequestDto requestDto) {
        Session session = authService.signup(requestDto.login(), requestDto.password());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponseDto(session.getApiToken()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto requestDto) {
        Session session = authService.login(requestDto.login(), requestDto.password());
        return ResponseEntity.ok(new AuthResponseDto(session.getApiToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        // Extract userId from token - for now, this is simplified
        // In a real application, you would validate the token and extract the userId
        return ResponseEntity.noContent().build();
    }
}
