package fr.univartois.butinfo.sae.abyss.social.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Group.
 *
 * This immutable record represents the data exchanged with clients for Group resources.
 * Validation annotations ensure incoming payloads respect business rules before mapping
 * to the domain entity.
 *
 * Field contracts:
 * - id: optional MongoDB ObjectId when present (read-only for creation).
 * - name: required, non-blank, between 3 and 50 characters.
 * - tags: required array, up to 10 tags.
 * - posts: required array of ObjectId representing post references (can be empty).
 * - createdAt: optional creation timestamp; if provided it must not be in the future.
 */
public record GroupDTO (
        ObjectId id,

        @NotBlank(message = "Groups name cannot be blank")
        @Size(min = 3, max = 50, message = "Groups name must be between 3 and 50 characters")
        String name,

        @NotNull(message = "Tags cannot be null")
        @Size(max = 10, message = "You can specify up to 10 tags")
        String[] tags,

        @NotNull(message = "Posts cannot be null")
        ObjectId[] posts,

        @PastOrPresent(message = "Creation date cannot be in the future")
        LocalDateTime createdAt
    ) {}
