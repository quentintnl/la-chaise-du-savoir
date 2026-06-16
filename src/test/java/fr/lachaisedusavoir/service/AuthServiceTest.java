package fr.lachaisedusavoir.service;

import fr.lachaisedusavoir.config.JwtUtil;
import fr.lachaisedusavoir.models.Session;
import fr.lachaisedusavoir.models.User;
import fr.lachaisedusavoir.repository.SessionRepository;
import fr.lachaisedusavoir.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void signup_shouldCreateUserAndSession() {

        when(userRepository.existsByLogin("john"))
                .thenReturn(false);

        when(passwordEncoder.encode("password"))
                .thenReturn("hashedPassword");

        User savedUser = new User("john", "hashedPassword");
        savedUser.setId(1);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        when(jwtUtil.generateToken("john"))
                .thenReturn("jwt-token");

        Session savedSession = new Session(savedUser, "jwt-token");

        when(sessionRepository.save(any(Session.class)))
                .thenReturn(savedSession);

        Session result = authService.signup("john", "password");

        assertNotNull(result);
        assertEquals("jwt-token", result.getApiToken());

        verify(userRepository).existsByLogin("john");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void signup_shouldThrowException_whenUserAlreadyExists() {

        when(userRepository.existsByLogin("john"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup("john", "password")
        );

        assertEquals(
                "User already exists with login: john",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void login_shouldCreateNewSession_whenCredentialsAreValid() {

        User user = new User("john", "hashedPassword");
        user.setId(1);

        when(userRepository.findByLogin("john"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password", "hashedPassword"))
                .thenReturn(true);

        when(jwtUtil.generateToken("john"))
                .thenReturn("new-token");

        Session session = new Session(user, "new-token");

        when(sessionRepository.save(any(Session.class)))
                .thenReturn(session);

        Session result = authService.login("john", "password");

        assertNotNull(result);
        assertEquals("new-token", result.getApiToken());

        verify(sessionRepository).deleteByUserId(1);
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() {

        when(userRepository.findByLogin("john"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login("john", "password")
        );

        assertEquals(
                "Invalid login or password",
                exception.getMessage()
        );

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void login_shouldThrowException_whenPasswordDoesNotMatch() {

        User user = new User("john", "hashedPassword");
        user.setId(1);

        when(userRepository.findByLogin("john"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password", "hashedPassword"))
                .thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login("john", "password")
        );

        assertEquals(
                "Invalid login or password",
                exception.getMessage()
        );

        verify(sessionRepository, never()).deleteByUserId(anyInt());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void logout_shouldDeleteUserSessions() {

        authService.logout(42);

        verify(sessionRepository).deleteByUserId(42);
    }

    @Test
    void getUserById_shouldReturnUser_whenExists() {

        User user = new User("john", "password");
        user.setId(42);

        when(userRepository.findById(42))
                .thenReturn(Optional.of(user));

        User result = authService.getUserById(42);

        assertNotNull(result);
        assertEquals(42, result.getId());
        assertEquals("john", result.getLogin());
    }

    @Test
    void getUserById_shouldThrowException_whenNotFound() {

        when(userRepository.findById(42))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.getUserById(42)
        );

        assertEquals(
                "Utilisateur non trouvé avec l'ID: 42",
                exception.getMessage()
        );
    }

    @Test
    void signup_shouldSaveUserWithEncodedPassword() {

        when(userRepository.existsByLogin("john"))
                .thenReturn(false);

        when(passwordEncoder.encode("password"))
                .thenReturn("encoded");

        User savedUser = new User("john", "encoded");
        savedUser.setId(1);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        when(jwtUtil.generateToken("john"))
                .thenReturn("token");

        when(sessionRepository.save(any(Session.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.signup("john", "password");

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User createdUser = captor.getValue();

        assertEquals("john", createdUser.getLogin());
        assertEquals("encoded", createdUser.getPassword());
    }
}