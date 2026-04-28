package fr.univartois.butinfo.sae.abyss.social.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public record PostDTO (
        String id,

        String userId,

        @NotBlank(message= "Content cannot be empty")
        @Size(max = 500, message = "Content cannot exceed 500 characters")
        String content,

        String image,

        @NotNull(message = "Comments tab cannot be null")
        List<CommentDTO> comments,

        @NotNull(message = "Likes tab cannot be null")
        List<UserResponseDTO> likes,

        @NotNull(message = "Dislikes tab cannot be null")
        List<UserResponseDTO> dislikes,

        @PastOrPresent(message="Creation date cannot be in the future")
        LocalDateTime createdAt

) {}