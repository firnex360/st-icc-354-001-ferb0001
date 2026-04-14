package com.hospedaje.auth.dto;

import com.hospedaje.auth.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Safe outbound representation of a User — never exposes the password hash.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private UUID   id;
    private String firstName;
    private String lastName;
    private String email;
    private Role   role;
}
