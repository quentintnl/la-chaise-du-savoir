package fr.lachaisedusavoir.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.lachaisedusavoir.dto.AuthRequestDto;
import fr.lachaisedusavoir.models.Session;
import fr.lachaisedusavoir.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void signup_shouldReturn400_whenIllegalArgumentException() throws Exception {
        when(authService.signup(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Login déjà utilisé"));

        AuthRequestDto request = new AuthRequestDto("john", "password");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Login déjà utilisé"));
    }

    @Test
    void signup_shouldReturn500_whenUnexpectedException() throws Exception {
        when(authService.signup(anyString(), anyString()))
                .thenThrow(new RuntimeException());

        AuthRequestDto request = new AuthRequestDto("john", "password");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message")
                        .value("Une erreur est survenue lors de l'inscription"));
    }

    @Test
    void login_shouldReturn401_whenCredentialsAreInvalid() throws Exception {
        when(authService.login(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Identifiants invalides"));

        AuthRequestDto request = new AuthRequestDto("john", "password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Identifiants invalides"));
    }

    @Test
    void login_shouldReturn500_whenUnexpectedException() throws Exception {
        when(authService.login(anyString(), anyString()))
                .thenThrow(new RuntimeException());

        AuthRequestDto request = new AuthRequestDto("john", "password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message")
                        .value("Une erreur est survenue lors de la connexion"));
    }

    @Test
    void logout_shouldCallService_whenAuthenticationContainsUserId() {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(42, null);

        authController.logout(authentication);

        verify(authService).logout(42);
    }

    @Test
    void logout_shouldNotCallService_whenAuthenticationIsNull() {
        authController.logout(null);

        verify(authService, never()).logout(anyInt());
    }

    @Test
    void logout_shouldNotCallService_whenPrincipalIsNotInteger() {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken("john", null);

        authController.logout(authentication);

        verify(authService, never()).logout(anyInt());
    }
}