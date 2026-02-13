package com.practica.clon_mocky.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio encargado de generar y validar tokens JWT.
 * <p>
 * Se usa para dos propósitos:
 * <ul>
 *   <li><b>Tokens de mock:</b> Generados cuando un mock requiere autenticación JWT.
 *       El token expira en la misma fecha que el mock.</li>
 *   <li><b>Tokens de sesión (dashboard):</b> Se podrían usar para autenticación
 *       API si se extiende en el futuro.</li>
 * </ul>
 * </p>
 */
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    /** Clave secreta para firmar los JWT (Base64 desde application.properties) */
    @Value("${jwt.secret}")
    private String secret;

    /** Tiempo de expiración por defecto en minutos (para tokens de sesión) */
    @Value("${jwt.expiration}")
    private int defaultExpiration;

    // ========================
    // Generación de Tokens
    // ========================

    /**
     * Genera un token JWT específico para un mock.
     * El token incluye el UUID del mock como claim personalizado y
     * su expiración coincide con la del mock.
     *
     * @param mockUuid        UUID del mock al que pertenece el token
     * @param fechaExpiracion fecha en la que el token (y el mock) expiran
     * @return el token JWT firmado
     */
    public String generarTokenParaMock(String mockUuid, LocalDateTime fechaExpiracion) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("mockUuid", mockUuid);
        claims.put("tipo", "mock_access");

        Date expiration = Date.from(fechaExpiracion.atZone(ZoneId.systemDefault()).toInstant());

        String token = Jwts.builder()
                .issuer("clon-mocky")
                .claims(claims)
                .subject(mockUuid)
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();

        logger.info("Token JWT generado para mock UUID: {} | Expira: {}", mockUuid, fechaExpiracion);
        return token;
    }

    /**
     * Genera un token JWT para un usuario (uso general / sesión API).
     *
     * @param username nombre del usuario
     * @param roles    roles del usuario separados por coma
     * @return el token JWT firmado
     */
    public String generarTokenParaUsuario(String username, String roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("tipo", "user_session");

        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(defaultExpiration);
        Date expDate = Date.from(expiracion.atZone(ZoneId.systemDefault()).toInstant());

        String token = Jwts.builder()
                .issuer("clon-mocky")
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(expDate)
                .signWith(getSigningKey())
                .compact();

        logger.info("Token JWT generado para usuario: {} | Expira en: {} min", username, defaultExpiration);
        return token;
    }

    // ========================
    // Validación de Tokens
    // ========================

    /**
     * Valida un token JWT para acceder a un mock.
     * Verifica que el token no haya expirado y que el claim 'mockUuid' coincida.
     *
     * @param token    token JWT proporcionado
     * @param mockUuid UUID del mock que se intenta acceder
     * @return true si el token es válido para ese mock
     */
    public boolean validarTokenMock(String token, String mockUuid) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenMockUuid = claims.getSubject();
            boolean noExpirado = !isTokenExpired(claims);

            return mockUuid.equals(tokenMockUuid) && noExpirado;
        } catch (ExpiredJwtException e) {
            logger.warn("Token expirado para mock UUID: {}", mockUuid);
            return false;
        } catch (Exception e) {
            logger.warn("Token inválido para mock UUID: {} - Error: {}", mockUuid, e.getMessage());
            return false;
        }
    }

    /**
     * Valida un token JWT de usuario.
     *
     * @param token token JWT
     * @return true si el token es válido y no ha expirado
     */
    public boolean validarToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !isTokenExpired(claims);
        } catch (Exception e) {
            logger.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrae el nombre de usuario (subject) de un token JWT.
     *
     * @param token token JWT
     * @return el username contenido en el subject del token
     */
    public String extraerUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extrae los roles de un token JWT de usuario.
     *
     * @param token token JWT
     * @return los roles como String separados por coma
     */
    public String extraerRoles(String token) {
        return extractAllClaims(token).get("roles", String.class);
    }

    /**
     * Extrae la fecha de expiración de un token JWT.
     *
     * @param token token JWT
     * @return la fecha de expiración
     */
    public Date extraerExpiracion(String token) {
        return extractAllClaims(token).getExpiration();
    }

    // ========================
    // Métodos Privados
    // ========================

    /**
     * Obtiene la clave de firma a partir del secret en Base64.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae todos los claims de un token JWT.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Verifica si un token ha expirado.
     */
    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
