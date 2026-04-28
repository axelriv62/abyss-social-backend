package fr.univartois.butinfo.sae.abyss.social.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record PostDTO (
        String id,

        String userId,

        @NotBlank(message= "Content cannot be empty")
        @Size(max = 500, message = "Content cannot exceed 500 characters")
        String content,

        String image,

        @NotNull(message = "Comments tab cannot be null")
        String[] comments,

        @NotNull(message = "Likes tab cannot be null")
        String[] likes,

        @NotNull(message = "Dislikes tab cannot be null")
        String[] dislikes,

        @PastOrPresent(message="Creation date cannot be in the future")
        LocalDateTime createdAt

) {}