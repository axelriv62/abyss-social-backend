package fr.univartois.butinfo.sae.abyss.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.bson.types.Binary;

/**
 * DTO for updating user basic profile information (username, profile picture).
 * This DTO allows authenticated users to update their profile without modifying sensitive data.
 */
public record UserUpdateRequestDTO(

        @NotBlank(message = "Username cannot be empty")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain alphanumeric characters, underscores, and hyphens")
        String username,

        Binary profilePicture
) { }
