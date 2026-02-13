package com.practica.clon_mocky.config;

import com.practica.clon_mocky.services.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad con doble cadena de filtros (dual SecurityFilterChain).
 * <p>
 * <b>Cadena 1 (Order 1) — Endpoints Mockeados ({@code /mock/**}):</b>
 * <ul>
 *   <li>Sin estado (STATELESS) — no usa sesión HTTP</li>
 *   <li>CSRF deshabilitado (es una API REST consumida externamente)</li>
 *   <li>Acceso público (permitAll) — la validación JWT se hace en el servicio</li>
 *   <li>Filtro {@link JwtAuthorizationFilter} antes de UsernamePasswordAuthenticationFilter</li>
 * </ul>
 * </p>
 * <p>
 * <b>Cadena 2 (Order 2) — Dashboard de Gestión ({@code /**}):</b>
 * <ul>
 *   <li>Con estado — sesión HTTP estándar</li>
 *   <li>Login por formulario ({@code /login})</li>
 *   <li>Rutas protegidas: {@code /admin/**} requiere ROLE_ADMIN</li>
 *   <li>Rutas públicas: {@code /login}, {@code /css/**}, {@code /js/**}, {@code /h2-console/**}</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UsuarioService usuarioService;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UsuarioService usuarioService,
                          JwtAuthorizationFilter jwtAuthorizationFilter,
                          PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
        this.passwordEncoder = passwordEncoder;
    }

    // ========================
    // Cadena 1: Mock API (JWT / Stateless)
    // ========================

    /**
     * Cadena de seguridad para los endpoints de consumo de mocks ({@code /mock/**}).
     * <p>
     * - Sin sesión (STATELESS): cada petición se autentica independientemente.
     * - CSRF deshabilitado: los mocks se consumen como API REST externa.
     * - Acceso público: qualquier cliente puede llamar al mock; la validación
     *   JWT se delega al {@link com.practica.clon_mocky.services.MockEndpointService}.
     * - Filtro JWT: establece contexto de autenticación si se envía un Bearer token.
     * </p>
     */
    @Bean
    @Order(1)
    public SecurityFilterChain mockApiFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/mock/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // ========================
    // Cadena 2: Dashboard (Sesión / Formulario)
    // ========================

    /**
     * Cadena de seguridad para el dashboard de gestión (todo lo que NO sea /mock/**).
     * <p>
     * - Autenticación por formulario con página de login personalizada.
     * - Sesión HTTP estándar para mantener al usuario logueado.
     * - Rutas públicas: login, recursos estáticos, consola H2.
     * - Rutas admin: {@code /admin/**} solo accesibles con ROLE_ADMIN.
     * - Todas las demás rutas requieren autenticación.
     * </p>
     */
    @Bean
    @Order(2)
    public SecurityFilterChain dashboardFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf
                        // Deshabilitar CSRF para la consola H2 (solo desarrollo)
                        .ignoringRequestMatchers("/h2-console/**")
                )
                .authorizeHttpRequests(auth -> auth
                        // Recursos públicos
                        .requestMatchers(
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/h2-console/**"
                        ).permitAll()
                        // Rutas de administración (solo ADMIN)
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")
                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )
                // Login por formulario
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                // Logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // Permitir frames para la consola H2
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .authenticationProvider(authenticationProvider())
                .build();
    }

    // ========================
    // Beans de Autenticación
    // ========================

    /**
     * AuthenticationProvider que usa JPA (UsuarioService) para validar credenciales.
     * Conecta Spring Security con nuestra base de datos de usuarios.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(usuarioService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * AuthenticationManager necesario para la configuración de autenticación.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
