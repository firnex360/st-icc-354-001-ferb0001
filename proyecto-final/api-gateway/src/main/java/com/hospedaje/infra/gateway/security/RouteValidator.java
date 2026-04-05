package com.hospedaje.infra.gateway.security;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Determines which request paths are "open" (public) and should bypass
 * JWT validation in the {@link AuthenticationFilter}.
 * <p>
 * Add new open path prefixes to the {@code OPEN_ENDPOINTS} list as needed.
 */
@Component
public class RouteValidator {

    /**
     * Path prefixes that do NOT require authentication.
     */
    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/auth/register",
            "/auth/login",
            "/actuator",
            "/eureka"
    );

    /**
     * Check whether the given path should be excluded from JWT validation.
     *
     * @param path the request URI path
     * @return {@code true} if the path is public
     */
    public boolean isOpenEndpoint(String path) {
        return OPEN_ENDPOINTS.stream()
                .anyMatch(path::startsWith);
    }
}
