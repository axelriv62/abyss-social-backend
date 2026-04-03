package fr.univartois.butinfo.sae.abyss.social.dto;

/**
 * Data Transfer Object for image generation requests.
 * This DTO encapsulates the prompt provided by the user for generating an image, allowing it to be transferred between layers of the application, such as from the controller to the service layer. The prompt is a string that describes the desired image content, which will be processed by the image generation service to create the corresponding image.
 * @param prompt The textual prompt used for image generation. This field is expected to contain a description of the desired image content, which will be sent to the image generation service to create the corresponding image based on the provided prompt.
 */
public record GenerationRequestDTO (
    String prompt
) {}
