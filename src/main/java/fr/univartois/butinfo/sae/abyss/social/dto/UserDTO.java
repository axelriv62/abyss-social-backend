package fr.univartois.butinfo.sae.abyss.social.dto;

import fr.univartois.butinfo.sae.abyss.social.model.ROLES;
import jakarta.validation.constraints.*;
import org.bson.types.Binary;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for User entity.
 * This record is used to transfer user data between layers of the application.
 * It includes validation constraints to ensure data integrity for MongoDB storage.
 */
public record UserDTO(
        // The unique identifier for the user
        String id,

        // The username of the user
        @NotBlank(message = "Username cannot be empty")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain alphanumeric characters, underscores, and hyphens")
        String username,

        // The email address of the user
        @Email(message = "Email must be a valid email address")
        String email,

        // The password of the user
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        // The role of the user
        ROLES role,

        // The profile picture of the user as binary data
        Binary profilePicture,

        // List of friend UserResponseDTOs
        List<UserResponseDTO> friends,

        // List of banned user UserResponseDTOs
        List<UserResponseDTO> usersBanned,

        // List of GroupDTOs the user belongs to
        List<GroupDTO> groups,

        // List of PageDTOs the user follows
        List<PageDTO> pages,

        // The timestamp when the user was created
        @PastOrPresent(message = "Creation date cannot be in the future")
        LocalDateTime createdAt

) { }
