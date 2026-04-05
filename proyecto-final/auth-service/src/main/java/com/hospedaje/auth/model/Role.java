package com.hospedaje.auth.model;

/**
 * Roles available in the hotel accommodation platform.
 * <ul>
 *   <li>{@code ADMIN}  — Full administrative access (manage properties, users, reports)</li>
 *   <li>{@code CLIENT} — Standard customer (make reservations, leave reviews)</li>
 * </ul>
 */
public enum Role {
    ADMIN,
    CLIENT
}
