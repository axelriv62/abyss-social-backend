package fr.univartois.butinfo.sae.abyss.social.dto;

import jakarta.validation.constraints.*;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public record PostDTO (
        ObjectId id,

        ObjectId userId,

        @NotBlank(message= "Content cannot be empty")
        @Size(max = 500, message = "Content cannot exceed 500 characters")
        String content,

        Binary image,

        @NotNull(message = "Comments tab cannot be null")
        ObjectId[] comments,

        @NotNull(message = "Likes tab cannot be null")
        ObjectId[] likes,

        @NotNull(message = "Dislikes tab cannot be null")
        ObjectId[] dislikes,

        @PastOrPresent(message="Creation date cannot be in the future")
        LocalDateTime createdAt

) {}
