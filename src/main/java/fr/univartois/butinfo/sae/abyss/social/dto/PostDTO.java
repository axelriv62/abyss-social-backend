package fr.univartois.butinfo.sae.abyss.social.dto;

import jakarta.validation.constraints.*;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public record PostDTO (
        ObjectId id,

        @NotNull(message= "UserID cannot be null")
        ObjectId userId,

        @NotBlank(message= "Content cannot be empty")
        @Max(value=500, message="Content cannot exceed 500 characters")
        String content,

        Binary image,

        @NotNull(message = "Comments tab cannot be null")
        ObjectId[] comments,

        @Min(value=0, message="Like count cannot be negative")
        int like,

        @Min(value=0, message="Dislike count cannot be negative")
        int dislike,

        @PastOrPresent(message="Creation date cannot be in the future")
        LocalDateTime createdAt

) {}
