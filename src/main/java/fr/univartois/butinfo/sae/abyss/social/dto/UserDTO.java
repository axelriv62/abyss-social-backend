package fr.univartois.butinfo.sae.abyss.social.dto;

import fr.univartois.butinfo.sae.abyss.social.model.ROLES;
import jakarta.validation.constraints.*;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for User entity.
 * This record is used to transfer user data between layers of the application.
 * It includes validation constraints to ensure data integrity for MongoDB storage.
 */
public record UserDTO(
        // The unique identifier for the user
        ObjectId id,

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

        // The profile picture of the user, stored as binary data
        Binary profilePicture,

        // List of friend ObjectIds as Strings
        List<ObjectId> friends,

        // List of banned user ObjectIds as Strings
        List<ObjectId> usersBanned,

        // List of group ObjectIds the user belongs to
        List<ObjectId> groups,

        // List of pages ObjectIds the user belongs to
        List<ObjectId> pages,

        // The timestamp when the user was created
        @PastOrPresent(message = "Creation date cannot be in the future")
        LocalDateTime createdAt

) { }
