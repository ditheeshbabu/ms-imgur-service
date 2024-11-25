package com.imgur.imgurservice.util;

import com.imgur.imgurservice.exception.AccessDeniedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for managing JWT tokens.
 * Provides methods for token generation, validation, and claim extraction.
 */
@Service
public class JwtTokenManager {

    /**
     * Secret key for signing and validating JWT tokens.
     * This should be externalized to a secure configuration.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token the JWT token
     * @return the username extracted from the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token the JWT token
     * @return the expiration date of the token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the JWT token.
     *
     * @param token          the JWT token
     * @param claimsResolver a function to resolve the desired claim
     * @param <T>            the type of the claim
     * @return the resolved claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a JWT token.
     *
     * @param token the JWT token
     * @return the claims contained in the token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            throw new AccessDeniedException("Invalid or expired token: " + e.getMessage());
        }
    }

    /**
     * Validates a JWT token.
     *
     * @param token the JWT token to validate
     */
    public void validateToken(final String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
        } catch (JwtException e) {
            throw new AccessDeniedException("Token validation failed");
        }
    }

    /**
     * Generates a JWT token for a given username.
     *
     * @param userName the username to include in the token
     * @return the generated JWT token
     */
    public String generateToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userName);
    }

    /**
     * Creates a JWT token with the given claims and subject.
     *
     * @param claims   the claims to include in the token
     * @param userName the username (subject) of the token
     * @return the generated JWT token
     */
    private String createToken(Map<String, Object> claims, String userName) {
        long expirationTime = 60 * 60 * 1000; // 1 hour in milliseconds

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Retrieves the signing key for JWT tokens.
     *
     * @return the signing key
     */
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}