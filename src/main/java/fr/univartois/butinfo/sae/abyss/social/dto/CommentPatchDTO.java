package fr.univartois.butinfo.sae.abyss.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentPatchDTO(
        @NotBlank(message = "text cannot be blank")
        @Size(max = 500, message = "text cannot exceed 500 characters")
        String text
) {
}

