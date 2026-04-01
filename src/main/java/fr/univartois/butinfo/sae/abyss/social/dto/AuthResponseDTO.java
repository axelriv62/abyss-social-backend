package fr.univartois.butinfo.sae.abyss.social.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for authentication responses.
 * Contains the authenticated user's information, the authentication token, and the token type.
 * @param user The authenticated user's information, represented as a UserResponseDTO.
 * @param token The authentication token, typically a JWT, that can be used for subsequent authenticated requests.
 * @param type The type of the token, usually "Bearer", indicating that the token should be included in the Authorization header of HTTP requests.
 */
public record AuthResponseDTO(

        @NotBlank(message = "User information cannot be empty")
        UserResponseDTO user,

        @NotBlank(message = "Token cannot be empty")
        String token,

        @NotBlank(message = "Token type cannot be empty")
        String type
) {}