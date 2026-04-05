package com.hospedaje.infra.gateway.security;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Global Gateway filter that intercepts every request and validates the JWT
 * in the {@code Authorization} header before routing to downstream services.
 * <p>
 * <b>Open paths</b> (no JWT required):
 * <ul>
 *   <li>{@code /api/auth/register}</li>
 *   <li>{@code /api/auth/login}</li>
 *   <li>{@code /actuator/**}</li>
 *   <li>{@code /eureka/**}</li>
 * </ul>
 * <p>
 * All other paths require a valid {@code Bearer <token>} header.
 * On success, the filter injects the user's email and role into request headers
 * so downstream services can use them without re-parsing the JWT.
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final RouteValidator routeValidator;

    public AuthenticationFilter(JwtUtil jwtUtil, RouteValidator routeValidator) {
        this.jwtUtil = jwtUtil;
        this.routeValidator = routeValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // ── Skip open endpoints ──────────────────────────────────────
        if (routeValidator.isOpenEndpoint(path)) {
            log.debug("Open endpoint, skipping JWT validation: {}", path);
            return chain.filter(exchange);
        }

        // ── Check for Authorization header ───────────────────────────
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            log.warn("Missing Authorization header for: {}", path);
            return onUnauthorized(exchange, "Missing Authorization header");
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Invalid Authorization header format for: {}", path);
            return onUnauthorized(exchange, "Authorization header must start with 'Bearer '");
        }

        // ── Extract and validate the token ───────────────────────────
        String token = authHeader.substring(7); // Remove "Bearer " prefix

        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Invalid or expired JWT for: {}", path);
            return onUnauthorized(exchange, "Invalid or expired JWT token");
        }

        // ── Token is valid: enrich the request with user info ────────
        Claims claims = jwtUtil.extractAllClaims(token);
        String email = claims.getSubject();
        String role = claims.get("role", String.class);

        log.debug("JWT validated — user: {}, role: {}, path: {}", email, role, path);

        // Forward user identity to downstream services via custom headers
        ServerHttpRequest enrichedRequest = request.mutate()
                .header("X-Auth-User-Email", email)
                .header("X-Auth-User-Role", role != null ? role : "")
                .build();

        return chain.filter(exchange.mutate().request(enrichedRequest).build());
    }

    /**
     * Return a 401 Unauthorized response with a JSON body.
     */
    private Mono<Void> onUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        String body = """
                {"status":401,"error":"Unauthorized","message":"%s"}
                """.formatted(message);

        return response.writeWith(Mono.just(
                response.bufferFactory().wrap(body.getBytes())
        ));
    }

    /**
     * Run this filter with high priority (before other filters).
     */
    @Override
    public int getOrder() {
        return -1;
    }
}
