package fr.univartois.butinfo.sae.abyss.social.dto;

/**
 * DTO for simple message responses.
 * Used to return status messages from API endpoints.
 */
public record MessageResponseDTO(
        String message
) { }