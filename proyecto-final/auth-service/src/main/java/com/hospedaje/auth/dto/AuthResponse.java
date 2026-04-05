package com.hospedaje.auth.dto;

import lombok.*;

/**
 * Response returned after a successful login or registration.
 * Contains the signed JWT and basic user metadata.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String email;
    private String role;
    private String message;
}
