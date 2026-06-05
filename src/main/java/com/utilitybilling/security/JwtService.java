package com.utilitybilling.security;

import com.utilitybilling.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/** Creates and validates JWT access and refresh tokens. */
@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secret;
    @Value("${app.jwt.access-token-minutes}")
    private long accessMinutes;
    @Value("${app.jwt.refresh-token-days}")
    private long refreshDays;

    public String accessToken(User user) {
        return token(user, Instant.now().plusSeconds(accessMinutes * 60));
    }

    public String refreshToken(User user) {
        return token(user, Instant.now().plusSeconds(refreshDays * 86400));
    }

    public String subject(String token) {
        return claims(token).getSubject();
    }

    public boolean valid(String token, String email) {
        return email.equals(subject(token)) && claims(token).getExpiration().after(new Date());
    }

    private String token(User user, Instant expiry) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claims(Map.of("role", user.getRole().name(), "userId", user.getId()))
                .issuedAt(new Date())
                .expiration(Date.from(expiry))
                .signWith(key())
                .compact();
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey key() {
        byte[] bytes = secret.length() > 60 ? Decoders.BASE64.decode(secret) : secret.getBytes();
        return Keys.hmacShaKeyFor(bytes);
    }
}
