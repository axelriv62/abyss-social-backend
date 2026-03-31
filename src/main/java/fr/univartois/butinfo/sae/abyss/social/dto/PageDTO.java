package fr.univartois.butinfo.sae.abyss.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public record PageDTO (
        ObjectId id,

        @NotNull(message = "User ID cannot be blank")
        ObjectId userId,

        @NotBlank(message = "Page name cannot be blank")
        @Size(min = 3, max = 50, message = "Page name must be between 3 and 50 characters")
        String name,

        @NotNull(message = "Tags cannot be null")
        @Size(max = 10, message = "You can specify up to 10 tags")
        String[] tags,

        @NotNull(message = "Posts cannot be null")
        ObjectId[] posts,

        @PastOrPresent(message = "Creation date cannot be in the future")
        LocalDateTime createdAt
)
{}
