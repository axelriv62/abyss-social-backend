package fr.univartois.butinfo.sae.abyss.social.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user login requests.
 * Contains the necessary information for authenticating a user, including email and password.
 * @param email The email for authentication, must be a valid email address.
 * @param password The password for authentication, must be at least 8 characters long.
 */
public record AuthLoginRequestDTO(

        @Email(message = "Email must be a valid email address")
        String email,

        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) {}
