package com.hospedaje.notifications.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inbound DTO for sending a plain-text or HTML email.
 * <p>
 * Used by the Auth Service (registration confirmation)
 * and Reservation Service (booking confirmation).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    /** Recipient email address. */
    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String to;

    /** Email subject line. */
    @NotBlank(message = "Subject is required")
    private String subject;

    /** Email body (plain text). */
    @NotBlank(message = "Body is required")
    private String body;
}
