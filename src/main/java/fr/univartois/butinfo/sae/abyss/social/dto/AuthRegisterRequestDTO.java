package fr.univartois.butinfo.sae.abyss.social.dto;

import jakarta.validation.constraints.*;
import org.bson.types.Binary;

/**
 * Data Transfer Object for user registration requests.
 * Contains the necessary information for registering a new user, including username, email, password, and an optional profile picture.
 * @param username The username for the new user, must be between 3 and 50 characters, and can only contain alphanumeric characters, underscores, and hyphens.
 * @param email The email for the new user, must be a valid email address.
 * @param password The password for the new user, must be at least 8 characters long.
 * @param profilePicture An optional profile picture for the new user, stored as a binary object.
 */
public record AuthRegisterRequestDTO(

        @NotBlank(message = "Username cannot be empty")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain alphanumeric characters, underscores, and hyphens")
        String username,

        @Email(message = "Email must be a valid email address")
        String email,

        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        Binary profilePicture
) {}
