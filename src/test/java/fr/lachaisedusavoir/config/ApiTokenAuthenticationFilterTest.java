package fr.lachaisedusavoir.config;

import fr.lachaisedusavoir.models.Session;
import fr.lachaisedusavoir.models.User;
import fr.lachaisedusavoir.repository.SessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiTokenAuthenticationFilterTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private ApiTokenAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setup() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldDoNothing_whenAuthorizationHeaderIsMissing()
            throws ServletException, IOException {

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(sessionRepository, never()).findByApiToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldDoNothing_whenAuthorizationHeaderIsNotBearer()
            throws ServletException, IOException {

        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic abc123");

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(sessionRepository, never()).findByApiToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldDoNothing_whenTokenIsEmpty()
            throws ServletException, IOException {

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer ");

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(sessionRepository, never()).findByApiToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldDoNothing_whenTokenNotFound()
            throws ServletException, IOException {

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");

        when(sessionRepository.findByApiToken("invalid-token"))
                .thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(sessionRepository).findByApiToken("invalid-token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldAuthenticateUser_whenTokenIsValid()
            throws ServletException, IOException {

        User user = new User();
        user.setId(42);

        Session session = new Session();
        session.setUser(user);
        session.setApiToken("valid-token");

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");

        when(sessionRepository.findByApiToken("valid-token"))
                .thenReturn(Optional.of(session));

        filter.doFilter(request, response, filterChain);

        var authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals(42, authentication.getPrincipal());
        assertEquals("valid-token", authentication.getCredentials());

        verify(sessionRepository).findByApiToken("valid-token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotReloadAuthentication_whenAlreadyAuthenticated()
            throws ServletException, IOException {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        1,
                        "existing-token"
                )
        );

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");

        filter.doFilter(request, response, filterChain);

        verify(sessionRepository, never()).findByApiToken(anyString());
        verify(filterChain).doFilter(request, response);

        assertEquals(
                1,
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal()
        );
    }

    @Test
    void shouldTrimTokenBeforeSearching()
            throws ServletException, IOException {

        User user = new User();
        user.setId(99);

        Session session = new Session();
        session.setUser(user);

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer    token123   "
        );

        when(sessionRepository.findByApiToken("token123"))
                .thenReturn(Optional.of(session));

        filter.doFilter(request, response, filterChain);

        verify(sessionRepository).findByApiToken("token123");
    }
}