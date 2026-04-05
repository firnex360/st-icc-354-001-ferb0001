package com.hospedaje.auth.controller;

import com.hospedaje.auth.dto.AuthResponse;
import com.hospedaje.auth.dto.LoginRequest;
import com.hospedaje.auth.dto.RegisterRequest;
import com.hospedaje.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 * <p>
 * Endpoints:
 * <ul>
 *   <li>{@code POST /auth/register} — Register a new CLIENT user and receive a JWT.</li>
 *   <li>{@code POST /auth/login}    — Authenticate and receive a JWT.</li>
 *   <li>{@code GET  /auth/validate} — (Health) Confirm the service is running.</li>
 * </ul>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ─── Registration ────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─── Login ───────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // ─── Health Check ────────────────────────────────────────────────

    @GetMapping("/validate")
    public ResponseEntity<String> validate() {
        return ResponseEntity.ok("Auth Service is running");
    }
}
