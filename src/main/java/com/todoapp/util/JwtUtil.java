// src/main/java/com/todoapp/util/JwtUtil.java
package com.todoapp.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Utility class for token creation, validation, and extraction.
 * Handles JWT operations for authentication and authorization.
 */
@Component
public class JwtUtil {

    // JWT Secret key (in production, use environment variable)
    @Value("${jwt.secret:mySecretKey1234567890abcdefghijklmnopqrstuvwxyz}")
    private String jwtSecret;

    // JWT expiration time (24 hours in milliseconds)
    @Value("${jwt.expiration:86400000}")
    private Long jwtExpirationMs;

    /**
     * Generate a secure secret key from the configured secret string
     * @return SecretKey for JWT signing
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT token for a user
     * @param userDetails User details for token creation
     * @return JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Generate JWT token with custom claims
     * @param extraClaims Additional claims to include in token
     * @param userDetails User details for token creation
     * @return JWT token string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return createToken(extraClaims, userDetails.getUsername());
    }

    /**
     * Create JWT token with claims and subject
     * @param claims Token claims
     * @param subject Token subject (username/email)
     * @return JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extract username from JWT token
     * @param token JWT token
     * @return Username/email from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from JWT token
     * @param token JWT token
     * @return Expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract a specific claim from JWT token
     * @param token JWT token
     * @param claimsResolver Function to extract specific claim
     * @param <T> Type of the claim
     * @return Extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from JWT token
     * @param token JWT token
     * @return All claims from the token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser() // Changed from parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("JWT token has expired", e);
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            throw new RuntimeException("JWT token is malformed", e);
        } catch (SecurityException e) {
            throw new RuntimeException("JWT signature validation failed", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("JWT token compact of handler are invalid", e);
        }
    }

    /**
     * Check if JWT token is expired
     * @param token JWT token
     * @return true if token is expired, false otherwise
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate JWT token against user details
     * @param token JWT token
     * @param userDetails User details to validate against
     * @return true if token is valid, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Validate JWT token format and signature
     * @param token JWT token
     * @return true if token is valid, false otherwise
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get JWT expiration time in milliseconds
     * @return Expiration time in milliseconds
     */
    public Long getExpirationTime() {
        return jwtExpirationMs;
    }

    /**
     * Generate refresh token (longer expiration)
     * @param userDetails User details for token creation
     * @return Refresh token string
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (jwtExpirationMs * 7)); // 7 days

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extract token type from claims
     * @param token JWT token
     * @return Token type (access/refresh)
     */
    public String extractTokenType(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("type", String.class);
    }

    /**
     * Check if token is a refresh token
     * @param token JWT token
     * @return true if it's a refresh token, false otherwise
     */
    public Boolean isRefreshToken(String token) {
        String tokenType = extractTokenType(token);
        return "refresh".equals(tokenType);
    }
}