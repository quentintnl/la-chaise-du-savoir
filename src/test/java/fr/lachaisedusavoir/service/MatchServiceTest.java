package fr.lachaisedusavoir.service;

import fr.lachaisedusavoir.models.GameMatch;
import fr.lachaisedusavoir.models.User;
import fr.lachaisedusavoir.repository.GameMatchRepository;
import fr.lachaisedusavoir.repository.RoundAnswerRepository;
import fr.lachaisedusavoir.repository.RoundsRepository;
import fr.lachaisedusavoir.repository.UserRepository;
import fr.lachaisedusavoir.repository.WinRepository;
import fr.lachaisedusavoir.repository.WinSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private GameMatchRepository gameMatchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoundsRepository roundsRepository;

    @Mock
    private RoundAnswerRepository roundAnswerRepository;

    @Mock
    private WinRepository winRepository;

    @Mock
    private WinSessionRepository winSessionRepository;

    @InjectMocks
    private MatchService matchService;

    @Test
    void createMatch_shouldCreateMatchSuccessfully() {

        User user = new User();
        user.setId(1);
        user.setLogin("john");

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        when(gameMatchRepository.save(any(GameMatch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GameMatch result = matchService.createMatch(1);

        assertNotNull(result);
        assertEquals(user, result.getUser1());
        assertFalse(result.getStatus());
        assertNotNull(result.getCreated_at());

        verify(gameMatchRepository).save(any(GameMatch.class));
    }

    @Test
    void createMatch_shouldGenerateInviteCode() {

        User user = new User();
        user.setId(1);

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        when(gameMatchRepository.save(any(GameMatch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        matchService.createMatch(1);

        ArgumentCaptor<GameMatch> captor =
                ArgumentCaptor.forClass(GameMatch.class);

        verify(gameMatchRepository).save(captor.capture());

        GameMatch savedMatch = captor.getValue();

        assertNotNull(savedMatch.getInviteCode());
        assertEquals(4, savedMatch.getInviteCode().length());
    }

    @Test
    void createMatch_shouldThrowException_whenUserNotFound() {

        when(userRepository.findById(1))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> matchService.createMatch(1)
        );

        assertEquals("User not found", exception.getMessage());

        verify(gameMatchRepository, never()).save(any());
    }

    @Test
    void joinMatch_shouldJoinMatchSuccessfully() {

        User user2 = new User();
        user2.setId(2);

        GameMatch match = new GameMatch();
        match.setInviteCode("1234");
        match.setStatus(false);

        when(gameMatchRepository.findByInviteCode("1234"))
                .thenReturn(Optional.of(match));

        when(userRepository.findById(2))
                .thenReturn(Optional.of(user2));

        when(gameMatchRepository.save(any(GameMatch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GameMatch result = matchService.joinMatch("1234", 2);

        assertEquals(user2, result.getUser2());
        assertTrue(result.getStatus());

        verify(gameMatchRepository).save(match);
    }

    @Test
    void joinMatch_shouldThrowException_whenMatchNotFound() {

        when(gameMatchRepository.findByInviteCode("9999"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> matchService.joinMatch("9999", 2)
        );

        assertEquals(
                "Match not found for code: 9999",
                exception.getMessage()
        );

        verify(gameMatchRepository, never()).save(any());
    }

    @Test
    void joinMatch_shouldThrowException_whenMatchAlreadyFull() {

        User existingUser = new User();
        existingUser.setId(99);

        GameMatch match = new GameMatch();
        match.setUser2(existingUser);

        when(gameMatchRepository.findByInviteCode("1234"))
                .thenReturn(Optional.of(match));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> matchService.joinMatch("1234", 2)
        );

        assertEquals(
                "Match is already full",
                exception.getMessage()
        );

        verify(gameMatchRepository, never()).save(any());
    }

    @Test
    void joinMatch_shouldThrowException_whenUserNotFound() {

        GameMatch match = new GameMatch();

        when(gameMatchRepository.findByInviteCode("1234"))
                .thenReturn(Optional.of(match));

        when(userRepository.findById(2))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> matchService.joinMatch("1234", 2)
        );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(gameMatchRepository, never()).save(any());
    }

    @Test
    void joinMatch_shouldActivateMatch() {

        User user = new User();
        user.setId(2);

        GameMatch match = new GameMatch();
        match.setStatus(false);

        when(gameMatchRepository.findByInviteCode("1234"))
                .thenReturn(Optional.of(match));

        when(userRepository.findById(2))
                .thenReturn(Optional.of(user));

        when(gameMatchRepository.save(any(GameMatch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GameMatch result = matchService.joinMatch("1234", 2);

        assertTrue(result.getStatus());
    }
}