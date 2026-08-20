package com.pradip.banksphere.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class JwtServiceTest {

    private final String TEST_SECRET = "uPc2arKhU9hkIKuWYQWcXRviW4adhxBMGIahhSprZUg=";

    private JwtService jwtService;

    @BeforeEach
    void setUp(){
        jwtService = new JwtService(TEST_SECRET, 900000L);
    }

    @Test
    public void generateToken_shouldGenerateValidToken(){
        String token = jwtService.generateToken("5",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        assertNotNull(token);
        assertFalse(token.isBlank());

        SecretKey secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(TEST_SECRET)
        );

    }
    @Test
    void generateToken_shouldContainCorrectClaims() {

        // Arrange
        String subject = "5";

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_CUSTOMER")
        );

        // Act
        String token = jwtService.generateToken(subject, authorities);

        Claims claims = jwtService.extractClaims(token);

        // Assert
        assertEquals("5", claims.getSubject());

        List<String> actualAuthorities =
                claims.get("authorities", List.class);

        assertEquals(
                List.of("ROLE_CUSTOMER"),
                actualAuthorities
        );
    }
    @Test
    void validateJwt_shouldReturnFalseForTamperedToken() {

        // Arrange
        String token = jwtService.generateToken(
                "5",
                List.of(
                        new SimpleGrantedAuthority("ROLE_CUSTOMER")
                )
        );

        // Tamper with the JWT payload
        String[] parts = token.split("\\.");

        String tamperedToken =
                parts[0] + "." +
                        parts[1] + "tampered" + "." +
                        parts[2];

        // Act
        boolean result = jwtService.validateJwt(tamperedToken);

        // Assert
        assertFalse(result);
    }

    @Test
    public void validateJwt_shouldReturnFalseForExpiredToken(){
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));

        Date issuedAt = new Date(System.currentTimeMillis() - 20_000);

        Date expiration = new Date(
                System.currentTimeMillis() - 10_000
        );

        String expiredToken = Jwts.builder()
                .subject("5")
                .claim("authorities",
                        List.of("ROLE_CUSTOMER"))
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();

        boolean result = jwtService.validateJwt(expiredToken);

        assertFalse(result);
    }
    @Test
    void extractClaims_shouldReturnCorrectClaims() {

        // Arrange
        String subject = "5";

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_CUSTOMER")
        );

        // Act
        String token = jwtService.generateToken(subject, authorities);

        Claims claims = jwtService.extractClaims(token);

        // Assert
        assertEquals("5", claims.getSubject());

        List<String> actualAuthorities =
                claims.get("authorities", List.class);

        assertEquals(
                List.of("ROLE_CUSTOMER"),
                actualAuthorities
        );

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

}
