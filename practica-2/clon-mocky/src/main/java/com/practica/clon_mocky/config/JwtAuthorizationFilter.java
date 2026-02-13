package com.practica.clon_mocky.config;

import com.practica.clon_mocky.services.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Filtro de autorización JWT para las peticiones a endpoints mockeados.
 * <p>
 * Solo se activa en las rutas {@code /mock/**}. Extrae el token Bearer
 * del header Authorization, lo valida con {@link JwtService} y establece
 * la autenticación en el SecurityContext para que Spring Security permita el acceso.
 * </p>
 * <p>
 * Este filtro NO se aplica a las rutas del dashboard (gestionadas por sesión).
 * </p>
 */
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    private final JwtService jwtService;

    public JwtAuthorizationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Solo aplicar este filtro a las rutas /mock/**
     * Las demás rutas usan autenticación por sesión.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/mock/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;

        // Extraer token del header Authorization: Bearer <token>
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
        }

        // Si hay token y no hay autenticación previa en el contexto, validar
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtService.validarToken(token)) {
                    String subject = jwtService.extraerUsername(token);

                    // Crear autenticación con rol genérico de acceso a mock
                    List<SimpleGrantedAuthority> authorities =
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_MOCK_ACCESS"));

                    UserDetails userDetails = new User(subject, "", true, true, true, true, authorities);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("JWT válido para mock access - Subject: {}", subject);
                }
            } catch (Exception e) {
                logger.warn("Error al procesar JWT: {}", e.getMessage());
                // No se establece autenticación; el mock service decidirá si rechazar
            }
        }

        filterChain.doFilter(request, response);
    }
}
