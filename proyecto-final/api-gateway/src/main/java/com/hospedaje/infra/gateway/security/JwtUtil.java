package com.hospedaje.infra.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT utility for the API Gateway.
 * <p>
 * Validates tokens issued by the Auth Service. Uses the same secret key
 * so it can verify the HMAC-SHA signature without calling the Auth Service.
 * <p>
 * ⚠️  The {@code jwt.secret} property MUST match the value configured
 * in the Auth Service's application.yml.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Validate the token: verify the signature, check expiration.
     *
     * @param token the raw JWT string (without "Bearer " prefix)
     * @return {@code true} if the token is valid and not expired
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            // Any parsing/signature error means the token is invalid
            return false;
        }
    }

    /**
     * Extract all claims from the token after verifying the signature.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract the subject (email) from the token.
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extract the role claim from the token.
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Derive the HMAC-SHA signing key from the Base64-encoded secret.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
