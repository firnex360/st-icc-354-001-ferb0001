package com.hospedaje.auth.service;

import com.hospedaje.auth.client.NotificationClient;
import com.hospedaje.auth.dto.AuthResponse;
import com.hospedaje.auth.dto.LoginRequest;
import com.hospedaje.auth.dto.RegisterRequest;
import com.hospedaje.auth.dto.UserDto;
import com.hospedaje.auth.model.Role;
import com.hospedaje.auth.model.User;
import com.hospedaje.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Business logic for user registration and authentication.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final NotificationClient notificationClient;

    // ─── Registration ────────────────────────────────────────────────

    /**
     * Register a new CLIENT user. Returns a JWT on success.
     *
     * @throws IllegalArgumentException if the email is already taken
     */
    public AuthResponse register(RegisterRequest request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        // Build and persist the new user
        String firstName = (request.getFirstName() != null && !request.getFirstName().isBlank())
                ? request.getFirstName() : "User";
        String lastName  = (request.getLastName()  != null && !request.getLastName().isBlank())
                ? request.getLastName()  : "";

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CLIENT)   // New registrations are always CLIENT
                .build();

        userRepository.save(user);
        log.info("New user registered: {} ({})", user.getEmail(), user.getRole());

        // Send welcome email — failure is non-fatal so registration still succeeds
        try {
            notificationClient.sendEmail(Map.of(
                    "to",      user.getEmail(),
                    "subject", "Welcome to Hotel Platform",
                    "body",    "Hi " + firstName + ",\n\n" +
                               "Your account has been created successfully.\n" +
                               "You can now log in with: " + user.getEmail() + "\n\n" +
                               "Welcome aboard!\n" +
                               "— Hotel Platform Team"
            ));
            log.info("Welcome email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.warn("Could not send welcome email to {}: {}", user.getEmail(), e.getMessage());
        }

        // Generate JWT for immediate login
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("User registered successfully")
                .build();
    }

    // ─── User Queries ────────────────────────────────────────────────

    /** Return every registered user as a safe DTO (no password). */
    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    /** Find a single user by email and return a safe DTO. */
    public Optional<UserDto> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::toDto);
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    // ─── Login ───────────────────────────────────────────────────────

    /**
     * Authenticate a user by email + password and return a signed JWT.
     *
     * @throws BadCredentialsException if credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        // Delegate to Spring Security's AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // If we reach here, authentication succeeded
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        String token = jwtService.generateToken(user);
        log.info("User logged in: {} ({})", user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }
}
