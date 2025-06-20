package com.atlas.jwt.service;

import com.atlas.user.repository.model.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtTokenProvider {

    private final String jwtSecret;
    private final int jwtExpirationInMs;
    private final SecretKey secretKey;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration-ms}") int jwtExpirationInMs) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationInMs = jwtExpirationInMs;
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        log.info("JwtTokenProvider initialized with expiration: {} ms", jwtExpirationInMs);
    }

    /**
     * Generate JWT token for authenticated user
     *
     * @param user The authenticated user
     * @return JWT token string
     */
    public String generateToken(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        Map<String, Object> claims = buildClaims(user);

        try {
            String token =
                    Jwts.builder()
                            .setClaims(claims)
                            .setSubject(user.getId().toString())
                            .setIssuedAt(now)
                            .setExpiration(expiryDate)
                            .signWith(secretKey, SignatureAlgorithm.HS512)
                            .compact();

            log.info(
                    "Generated JWT token for user: id={}, email={}, expires={}",
                    user.getId(),
                    user.getEmail(),
                    expiryDate);

            return token;
        } catch (Exception e) {
            log.error(
                    "Failed to generate JWT token for user: id={}, email={}",
                    user.getId(),
                    user.getEmail(),
                    e);
            throw new JwtTokenGenerationException("Failed to generate JWT token", e);
        }
    }

    /**
     * Validate JWT token
     *
     * @param token JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.debug("JWT token validation failed: token is null or empty");
            return false;
        }

        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.debug("JWT token validation failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during JWT token validation: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract user ID from JWT token
     *
     * @param token JWT token
     * @return User ID
     * @throws JwtTokenParsingException if token is invalid
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            log.error("Failed to extract user ID from JWT token: {}", e.getMessage());
            throw new JwtTokenParsingException("Failed to extract user ID from token", e);
        }
    }

    /**
     * Extract email from JWT token
     *
     * @param token JWT token
     * @return User email
     * @throws JwtTokenParsingException if token is invalid
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.get("email", String.class);
        } catch (Exception e) {
            log.error("Failed to extract email from JWT token: {}", e.getMessage());
            throw new JwtTokenParsingException("Failed to extract email from token", e);
        }
    }

    /**
     * Extract expiration date from JWT token
     *
     * @param token JWT token
     * @return Expiration date
     * @throws JwtTokenParsingException if token is invalid
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.error("Failed to extract expiration date from JWT token: {}", e.getMessage());
            throw new JwtTokenParsingException("Failed to extract expiration date from token", e);
        }
    }

    /**
     * Check if JWT token is expired
     *
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.debug("Error checking token expiration, treating as expired: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Get token expiration time in milliseconds
     *
     * @return Expiration time in milliseconds
     */
    public int getExpirationTimeMs() {
        return jwtExpirationInMs;
    }

    /** Build claims map for JWT token */
    private Map<String, Object> buildClaims(UserEntity user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());

        // Optional claims - only add if not null
        if (user.getProfilePictureUrl() != null) {
            claims.put("profilePicture", user.getProfilePictureUrl());
        }

        return claims;
    }

    /** Get all claims from JWT token */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
