package com.hospedaje.auth.config;

import com.hospedaje.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for the Auth Microservice.
 * <p>
 * Key decisions:
 * <ul>
 *   <li>Stateless sessions — no server-side session; each request carries its own JWT.</li>
 *   <li>Open endpoints: {@code /auth/register} and {@code /auth/login} are public.</li>
 *   <li>BCrypt password encoding with default strength (10 rounds).</li>
 *   <li>Uses the modern {@link SecurityFilterChain} bean approach instead of the
 *       deprecated {@code WebSecurityConfigurerAdapter}.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    // ─── Security Filter Chain ───────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless JWT-based APIs
            .csrf(csrf -> csrf.disable())

            // Endpoint authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public: registration, login, actuator health, and user-query
                // endpoints. JWT validation for /auth/users/** is already enforced
                // by the API Gateway before the request reaches this service.
                .requestMatchers(
                    "/auth/register",
                    "/auth/login",
                    "/auth/users",
                    "/auth/users/me",
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()
                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // Stateless session management — no HTTP session created
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Set the custom authentication provider
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    // ─── Authentication Provider ─────────────────────────────────────

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ─── UserDetailsService (loads users from the database) ──────────

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));
    }

    // ─── Password Encoder ────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ─── Authentication Manager ──────────────────────────────────────

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
