package com.pradip.banksphere.service.jwt;

import com.pradip.banksphere.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final String secretKey;
    private final long expiration;

    public JwtService(
            @Value("${jwt.secret.key}") String secretKey,
            @Value("${jwt.expiration}") long expiration) {

        this.secretKey = secretKey;
        this.expiration = expiration;
    }


    public String generateToken(
            String subject,
            Collection<? extends GrantedAuthority> authorities) {

        List<String> authorityList = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Date issuedAt = new Date();
        Date expirationDate =
                new Date(System.currentTimeMillis() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secretKey)
        );

        return Jwts.builder()
                .subject(subject)
                .claim("authorities", authorityList)
                .issuedAt(issuedAt)
                .expiration(expirationDate)
                .signWith(key)
                .compact();
    }

    public boolean validateJwt(String token) {
        try{
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
            Jwts.parser().verifyWith(key)
                    .build().parseSignedClaims(token);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    public Claims extractClaims(String token){
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        return Jwts.parser()
                 .verifyWith(key)
                 .build()
                 .parseSignedClaims(token)
                 .getPayload();
    }

}
