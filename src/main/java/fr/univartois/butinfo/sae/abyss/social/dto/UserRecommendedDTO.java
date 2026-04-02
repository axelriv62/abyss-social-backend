package fr.univartois.butinfo.sae.abyss.social.dto;

import fr.univartois.butinfo.sae.abyss.social.model.ROLES;
import jakarta.validation.constraints.*;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for recommended users.
 * This record is used to transfer recommended user data between layers of the application.
 */
public record UserRecommendedDTO(
        // The unique identifier for the recommended user
        ObjectId id,

        // The recommendation score
        @PositiveOrZero(message = "Score must be zero or positive")
        double score,

        // The number of shared friends
        @PositiveOrZero(message = "Shared friends must be zero or positive")
        int sharedFriends,

        // The number of shared groups
        @PositiveOrZero(message = "Shared groups must be zero or positive")
        int sharedGroups,

        // The number of shared pages
        @PositiveOrZero(message = "Shared pages must be zero or positive")
        int sharedPages,

        // The username of the recommended user
        @NotBlank(message = "Username cannot be empty")
        String username,

        // The email address of the recommended user
        @Email(message = "Email must be a valid email address")
        String email,

        // The role of the recommended user
        ROLES role,

        // The timestamp when the recommended user was created
        @PastOrPresent(message = "Creation date cannot be in the future")
        LocalDateTime createdAt
) { }