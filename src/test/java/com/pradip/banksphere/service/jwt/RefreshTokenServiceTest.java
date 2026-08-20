package com.pradip.banksphere.service.jwt;

import com.pradip.banksphere.entity.refresh.RefreshToken;
import com.pradip.banksphere.exception.InvalidCredentialsException;
import com.pradip.banksphere.repository.refresh.RefreshTokenRepository;
import com.pradip.banksphere.service.refresh.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Ref;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    public void findByRawToken_shouldReturnRefreshToken_whenTokenExists(){
        String rawToken = "xBynXFUT_LYSMezy4vSQFH4DXBAwpwSMbg_-x4wMpJE";

        RefreshToken refreshToken = new RefreshToken();

        String hash = refreshTokenService.hashToken(rawToken);

        when(refreshTokenRepository.findByTokenHash(hash))
                .thenReturn(Optional.of(refreshToken));

        RefreshToken result = refreshTokenService.findByRawToken(rawToken);

        assertNotNull(result);
        assertEquals(refreshToken, result);

    }

    @Test
    void findByRawToken_shouldThrowException_whenTokenDoesNotExist() {

        // Arrange
        String rawToken =
                "xBynXFUT_LYSMezy4vSQFH4DXBAwpwSMbg_-x4wMpJE";

        String hash = refreshTokenService.hashToken(rawToken);

        when(refreshTokenRepository.findByTokenHash(hash))
                .thenReturn(Optional.empty());

        // Act + Assert
        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> refreshTokenService.findByRawToken(rawToken)
        );

        assertEquals("Invalid token", exception.getMessage());
    }

    @Test
    void findByRawToken_shouldCallRepositoryWithHashedToken() {

        // Arrange
        String rawToken =
                "xBynXFUT_LYSMezy4vSQFH4DXBAwpwSMbg_-x4wMpJE";

        String expectedHash =
                refreshTokenService.hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken();

        when(refreshTokenRepository.findByTokenHash(expectedHash))
                .thenReturn(Optional.of(refreshToken));

        // Act
        refreshTokenService.findByRawToken(rawToken);

        // Assert
        verify(refreshTokenRepository)
                .findByTokenHash(expectedHash);
    }

    @Test
    void revokeRefreshToken_shouldRevokeAndSaveToken() {

        // Arrange
        RefreshToken refreshToken = new RefreshToken();

        assertFalse(refreshToken.isRevoked());

        // Act
        refreshTokenService.revokeRefreshToken(refreshToken);

        // Assert
        assertTrue(refreshToken.isRevoked());

        verify(refreshTokenRepository)
                .save(refreshToken);
    }

    @Test
    void validateRefreshToken_shouldReturnToken_whenTokenIsValid() {

        // Arrange
        String rawToken = "test-refresh-token";

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(
                LocalDateTime.now().plusDays(7)
        );

        String hash = refreshTokenService.hashToken(rawToken);

        when(refreshTokenRepository.findByTokenHash(hash))
                .thenReturn(Optional.of(refreshToken));

        // Act
        RefreshToken result =
                refreshTokenService.validateRefreshToken(rawToken);

        // Assert
        assertNotNull(result);
        assertSame(refreshToken, result);
    }

    @Test
    void validateRefreshToken_shouldThrowException_whenTokenIsRevoked() {

        // Arrange
        String rawToken = "test-refresh-token";

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRevoked(true);
        refreshToken.setExpiresAt(
                LocalDateTime.now().plusDays(7)
        );

        String hash = refreshTokenService.hashToken(rawToken);

        when(refreshTokenRepository.findByTokenHash(hash))
                .thenReturn(Optional.of(refreshToken));

        // Act + Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> refreshTokenService.validateRefreshToken(rawToken)
        );

        assertEquals("Invalid token", exception.getMessage());
    }
    @Test
    void validateRefreshToken_shouldThrowException_whenTokenIsExpired() {

        // Arrange
        String rawToken = "test-refresh-token";

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(
                LocalDateTime.now().minusDays(1)
        );

        String hash = refreshTokenService.hashToken(rawToken);

        when(refreshTokenRepository.findByTokenHash(hash))
                .thenReturn(Optional.of(refreshToken));

        // Act + Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> refreshTokenService.validateRefreshToken(rawToken)
        );

        assertEquals("Invalid token", exception.getMessage());
    }


}
