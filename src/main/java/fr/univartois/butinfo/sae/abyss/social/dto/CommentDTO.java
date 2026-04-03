package fr.univartois.butinfo.sae.abyss.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public record CommentDTO(
        ObjectId id,
        ObjectId userId,
        @NotBlank(message = "text cannot be blank")
        String text,
        @PastOrPresent(message = "Creation date cannot be in the future")
        LocalDateTime createdAt
) {
}
