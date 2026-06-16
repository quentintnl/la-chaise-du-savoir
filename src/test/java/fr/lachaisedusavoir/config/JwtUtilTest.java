package fr.lachaisedusavoir.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    /**
     * Clé HS512 >= 64 bytes obligatoire.
     */
    private static final String SECRET =
            "my-super-secret-key-for-jwt-tests-at-least-sixty-four-bytes-long-123456789";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        ReflectionTestUtils.setField(
                jwtUtil,
                "jwtSecret",
                SECRET
        );

        ReflectionTestUtils.setField(
                jwtUtil,
                "jwtExpiration",
                3600000L
        );
    }

    @Test
    void generateToken_shouldReturnToken() {

        String token = jwtUtil.generateToken("john");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void generateToken_shouldContainLogin() {

        String token = jwtUtil.generateToken("john");

        String login = jwtUtil.getLoginFromToken(token);

        assertEquals("john", login);
    }

    @Test
    void validateToken_shouldReturnTrue_forValidToken() {

        String token = jwtUtil.generateToken("john");

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnFalse_forInvalidToken() {

        assertFalse(jwtUtil.validateToken("invalid-token"));
    }

    @Test
    void getLoginFromToken_shouldReturnCorrectLogin() {

        String token = jwtUtil.generateToken("lachaise");

        String login = jwtUtil.getLoginFromToken(token);

        assertEquals("lachaise", login);
    }

    @Test
    void tokenShouldContainExpectedSubject() {

        String token = jwtUtil.generateToken("john");

        Claims claims = Jwts.parser()
                .verifyWith(
                        io.jsonwebtoken.security.Keys
                                .hmacShaKeyFor(SECRET.getBytes())
                )
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("john", claims.getSubject());
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsTampered() {

        String token = jwtUtil.generateToken("john");

        String tamperedToken =
                token.substring(0, token.length() - 2) + "ab";

        assertFalse(jwtUtil.validateToken(tamperedToken));
    }

    @Test
    void getLoginFromToken_shouldThrowException_forInvalidToken() {

        assertThrows(
                Exception.class,
                () -> jwtUtil.getLoginFromToken("invalid-token")
        );
    }

    @Test
    void generatedTokenShouldHaveExpirationDate() {

        String token = jwtUtil.generateToken("john");

        Claims claims = Jwts.parser()
                .verifyWith(
                        io.jsonwebtoken.security.Keys
                                .hmacShaKeyFor(SECRET.getBytes())
                )
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertNotNull(claims.getExpiration());
    }
}