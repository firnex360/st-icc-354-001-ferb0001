package com.hotel.frontend.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Collections;
import java.util.Map;

/**
 * Lightweight JWT payload reader for the frontend.
 * Decodes the claims without verifying the signature — signature
 * validation is already handled by the API Gateway.
 */
public class JwtParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Map<String, Object> getClaims(String token) {
        if (token == null || token.isBlank()) return Collections.emptyMap();
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return Collections.emptyMap();
            byte[] decoded = Base64.getUrlDecoder().decode(padBase64(parts[1]));
            return MAPPER.readValue(decoded, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    /** Returns the email stored in the JWT subject claim. */
    public static String getEmail(String token) {
        return (String) getClaims(token).getOrDefault("sub", "");
    }

    /** Returns the role claim, e.g. {@code "ROLE_ADMIN"} or {@code "ROLE_CLIENT"}. */
    public static String getRole(String token) {
        return (String) getClaims(token).getOrDefault("role", "");
    }

    private static String padBase64(String base64url) {
        int padding = (4 - base64url.length() % 4) % 4;
        return base64url + "=".repeat(padding);
    }
}
