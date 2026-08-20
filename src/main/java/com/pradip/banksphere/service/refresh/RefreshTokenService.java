package com.pradip.banksphere.service.refresh;

import com.pradip.banksphere.entity.refresh.RefreshToken;
import com.pradip.banksphere.entity.user.User;
import com.pradip.banksphere.exception.InvalidCredentialsException;
import com.pradip.banksphere.repository.refresh.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public String createRefreshToken(User user){
        String rawToken = generateRefreshToken();
        String hashToken = hashToken(rawToken);
        LocalDateTime createdAt = LocalDateTime.now();

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(hashToken)
                .user(user)
                .createdAt(createdAt)
                .expiresAt(createdAt.plusDays(7))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    public String generateRefreshToken(){
        SecureRandom secureRandom = new SecureRandom();

        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    public String hashToken(String token) {

        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = messageDigest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm not available", e
            );
        }
    }

    public RefreshToken findByRawToken(String rawToken){
        String tokenHash = hashToken(rawToken);

        return refreshTokenRepository.findByTokenHash(tokenHash).orElseThrow(
                () -> new SecurityException("Invalid token")
        );
    }

    public RefreshToken validateRefreshToken(String rawToken) {
       RefreshToken refreshToken = findByRawToken(rawToken);
        if (!refreshToken.isRevoked() && refreshToken.getExpiresAt().isAfter(LocalDateTime.now())){
            return refreshToken;
        }
        throw new InvalidCredentialsException("Invalid token");
    }

    public void revokeRefreshToken(RefreshToken refreshToken){

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

    }


}
