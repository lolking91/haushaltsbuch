package de.haushaltsbuch.backend.dto;

import jakarta.validation.constraints.NotBlank;


/**
 * Request body for {@code POST /api/auth/login}.
 *
 * @param username the username to authenticate
 * @param password the plain-text password to verify
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
