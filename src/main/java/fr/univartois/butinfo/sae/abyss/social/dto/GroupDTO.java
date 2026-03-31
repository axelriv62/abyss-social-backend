package fr.univartois.butinfo.sae.abyss.social.dto;

import jakarta.validation.constraints.*;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public record GroupDTO (
        ObjectId id,

        ObjectId userId,

        @NotBlank(message = "Groups name cannot be blank")
        @Size(min = 3, max = 50, message = "Groups name must be between 3 and 50 characters")
        String name,

        @NotNull(message = "Tags cannot be null")
        @Size(max = 10, message = "You can specify up to 10 tags")
        String[] tags,

        @NotEmpty(message = "Members cannot be empty")
        String[] posts,

        @PastOrPresent(message = "Creation date cannot be in the future")
        LocalDateTime createdAt
) {}
