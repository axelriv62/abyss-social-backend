package fr.univartois.butinfo.sae.abyss.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for password change requests.
 * Contains only the new password since authentication is verified via JWT token.
 * @param newPassword The new password to be set (must be at least 8 characters).
 */
public record ChangePasswordRequestDTO(
        @NotBlank(message = "New password cannot be empty")
        @Size(min = 8, message = "New password must be at least 8 characters long")
        String newPassword
) { }