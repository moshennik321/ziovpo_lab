package org.example.server.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Getter
    @Value("${jwt.accessExpirationMs}")
    private long accessExpirationMs;

    @Getter
    @Value("${jwt.refreshExpirationMs}")
    private long refreshExpirationMs;

    private Key key;

    @PostConstruct
    void init() {
        // Для HS256 нужен достаточно длинный secret (желательно 32+ символа)
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String subjectEmail) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(accessExpirationMs);

        return Jwts.builder()
                .setSubject(subjectEmail)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("type", "access")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public RefreshTokenData generateRefreshToken(String subjectEmail) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(refreshExpirationMs);
        String jti = UUID.randomUUID().toString().replace("-", "");

        String token = Jwts.builder()
                .setSubject(subjectEmail)
                .setId(jti)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("type", "refresh")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new RefreshTokenData(token, jti, exp);
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public record RefreshTokenData(String token, String jti, Instant expiresAt) {}
}