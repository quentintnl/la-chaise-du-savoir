package fr.lachaisedusavoir.config;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldDoNothing_whenAuthorizationHeaderMissing()
            throws ServletException, IOException {

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(jwtUtil);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldDoNothing_whenHeaderIsNotBearer()
            throws ServletException, IOException {

        request.addHeader("Authorization", "Basic token");

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(jwtUtil);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldDoNothing_whenTokenIsInvalid()
            throws ServletException, IOException {

        request.addHeader("Authorization", "Bearer invalid");

        when(jwtUtil.validateToken("invalid"))
                .thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtUtil).validateToken("invalid");
        verify(jwtUtil, never()).getLoginFromToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldAuthenticateUser_whenTokenIsValid()
            throws ServletException, IOException {

        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtUtil.validateToken("valid-token"))
                .thenReturn(true);

        when(jwtUtil.getLoginFromToken("valid-token"))
                .thenReturn("john");

        filter.doFilter(request, response, filterChain);

        var authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals("john", authentication.getPrincipal());

        verify(jwtUtil).validateToken("valid-token");
        verify(jwtUtil).getLoginFromToken("valid-token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticate_whenLoginIsNull()
            throws ServletException, IOException {

        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtUtil.validateToken("valid-token"))
                .thenReturn(true);

        when(jwtUtil.getLoginFromToken("valid-token"))
                .thenReturn(null);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtUtil).validateToken("valid-token");
        verify(jwtUtil).getLoginFromToken("valid-token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotOverrideExistingAuthentication()
            throws ServletException, IOException {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "existing-user",
                        null
                )
        );

        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtUtil.validateToken("valid-token"))
                .thenReturn(true);

        when(jwtUtil.getLoginFromToken("valid-token"))
                .thenReturn("john");

        filter.doFilter(request, response, filterChain);

        assertEquals(
                "existing-user",
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal()
        );

        verify(jwtUtil).validateToken("valid-token");
        verify(jwtUtil).getLoginFromToken("valid-token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldIgnoreJwtException()
            throws ServletException, IOException {

        request.addHeader("Authorization", "Bearer invalid");

        when(jwtUtil.validateToken("invalid"))
                .thenThrow(new JwtException("Token expired"));

        assertDoesNotThrow(() ->
                filter.doFilter(request, response, filterChain)
        );

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldIgnoreUnexpectedException()
            throws ServletException, IOException {

        request.addHeader("Authorization", "Bearer token");

        when(jwtUtil.validateToken("token"))
                .thenThrow(new RuntimeException("Unexpected error"));

        assertDoesNotThrow(() ->
                filter.doFilter(request, response, filterChain)
        );

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldExtractTokenCorrectly()
            throws ServletException, IOException {

        request.addHeader("Authorization", "Bearer my-token");

        when(jwtUtil.validateToken("my-token"))
                .thenReturn(false);

        filter.doFilter(request, response, filterChain);

        verify(jwtUtil).validateToken("my-token");
    }
}