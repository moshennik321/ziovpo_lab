package org.example.server.auth;

import org.example.server.auth.dto.*;
import org.example.server.security.JwtTokenProvider;
import org.example.server.session.UserSession;
import org.example.server.session.UserSessionRepository;
import org.example.server.user.*;
import java.util.regex.Pattern;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ApplicationUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserSessionRepository sessionRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(RegisterRequest req) {

        validatePassword(req.getPassword());

        String email = req.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        Role roleUser = roleRepository.findByName(UserRole.ROLE_USER.name())
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found in DB"));

        ApplicationUser user = ApplicationUser.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .enabled(true)
                .build();

        user.getRoles().add(roleUser);
        userRepository.save(user);

        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest req) {
        String email = normalizeEmail(req.getEmail());

        // Проверит пароль и существование пользователя через UserDetailsService
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, req.getPassword())
        );

        ApplicationUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return issueTokens(user);
    }

    public AuthResponse refresh(RefreshRequest req) {
        String refreshToken = req.getRefreshToken();

        try {
            Claims claims = jwtTokenProvider.parse(refreshToken).getBody();

            String type = claims.get("type", String.class);
            if (!"refresh".equals(type)) {
                throw new IllegalArgumentException("Invalid refresh token type");
            }

            String email = claims.getSubject();
            String jti = claims.getId();

            UserSession session = sessionRepository.findByRefreshJti(jti)
                    .orElseThrow(() -> new IllegalArgumentException("Refresh session not found"));

            if (session.isRevoked()) {
                throw new IllegalArgumentException("Refresh revoked");
            }

            if (session.getExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
                session.setRevoked(true);
                sessionRepository.save(session);
                throw new IllegalArgumentException("Refresh expired");
            }

            String incomingHash = sha256Hex(refreshToken);
            if (!incomingHash.equals(session.getRefreshTokenHash())) {
                session.setRevoked(true);
                sessionRepository.save(session);
                throw new IllegalArgumentException("Refresh token mismatch");
            }

            // Rotation: старый refresh — в revoked
            session.setRevoked(true);
            sessionRepository.save(session);

            ApplicationUser user = userRepository.findByEmail(normalizeEmail(email))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            return issueTokens(user);

        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }

    private AuthResponse issueTokens(ApplicationUser user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        var refreshData = jwtTokenProvider.generateRefreshToken(user.getEmail());

        UserSession newSession = UserSession.builder()
                .user(user)
                .refreshJti(refreshData.jti())
                .refreshTokenHash(sha256Hex(refreshData.token()))
                .expiresAt(OffsetDateTime.ofInstant(refreshData.expiresAt(), ZoneOffset.UTC))
                .revoked(false)
                .build();

        sessionRepository.save(newSession);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshData.token())
                .build();
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Hash error", e);
        }
    }
    private void validatePassword(String password) {
        if (password == null ||
                password.length() < 8 ||
                !Pattern.compile("[A-Z]").matcher(password).find() ||
                !Pattern.compile("[a-z]").matcher(password).find() ||
                !Pattern.compile("[0-9]").matcher(password).find() ||
                !Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(password).find()) {

            throw new IllegalArgumentException(
                    "Password must contain at least 8 chars, uppercase, lowercase, number, and special symbol."
            );
        }
    }
}