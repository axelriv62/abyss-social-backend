package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.GenerationRequestDTO;
import fr.univartois.butinfo.sae.abyss.social.dto.MessageResponseDTO;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.service.GenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller responsible for handling image generation requests. It provides endpoints for generating profile pictures and post images based on user-provided prompts.
 * The controller ensures that only authenticated users can access these endpoints and that they can only generate images for their own profiles and posts.
 * It uses the GenerationService to perform the actual image generation logic and returns appropriate responses based on the success or failure of the operations.
 */
@RestController
@RequestMapping("/generate")
public class GenerationController {

    /**
     * Service used to handle image generation logic. It is injected through the constructor and provides methods for generating profile pictures and post images based on user prompts.
     * The controller relies on this service to perform the actual generation of images and to update the corresponding user profiles and posts in the database after generation.
     */
    private final GenerationService generationService;

    /**
     * Constructor for the GenerationController class. It initializes the controller with the GenerationService, which is injected through the constructor.
     * This service is used to handle the image generation logic for both profile pictures and post images, allowing the controller to delegate the actual generation process to the service while focusing on handling HTTP requests and responses.
     * @param generationService The GenerationService used to handle image generation logic. This service is injected through the constructor and provides methods for generating profile pictures and post images based on user prompts, allowing the controller to delegate the actual generation process to the service while focusing on handling HTTP requests and responses.
     */
    public GenerationController(GenerationService generationService) {
        this.generationService = generationService;
    }

    /**
     * Endpoint for generating a profile picture based on a user-provided prompt.
     * This endpoint accepts a POST request with a JSON body containing the prompt, and it requires the user to be authenticated.
     * @param request The GenerationRequestDTO containing the prompt for image generation. This DTO is expected to have a "prompt" field that provides the textual input for generating the profile picture.
     * @param currentUser The currently authenticated user, injected by Spring Security using the @AuthenticationPrincipal annotation. This user is used to ensure that the profile picture is generated for the correct user and to update the user's profile with the generated image.
     * @return A ResponseEntity containing a MessageResponseDTO with the result of the operation. If the user is not authenticated, it returns a 401 Unauthorized response. If the generation is successful, it returns a 200 OK response with a success message.
     *         If there is an error during generation (e.g., invalid prompt), it returns a 400 Bad Request response with the error message.
     */
    @PostMapping("/profile-picture")
    @Operation(summary = "Generate a profile picture based on a user-provided prompt")
    @ApiResponse(responseCode = "200", description = "Profile picture generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid prompt or error during generation")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<MessageResponseDTO> generateProfilePicture(@RequestBody GenerationRequestDTO request, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDTO("Unauthorized"));
        }
        try {
            generationService.generateProfilePicture(currentUser, request.prompt());
            return ResponseEntity.ok(new MessageResponseDTO("Profile picture generated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * Endpoint for generating a post image based on a user-provided prompt.
     * This endpoint accepts a POST request with a JSON body containing the prompt and a path variable for the post ID.
     * It requires the user to be authenticated and ensures that the user is the author
     * @param request The GenerationRequestDTO containing the prompt for image generation. This DTO is expected to have a "prompt" field that provides the textual input for generating the post image.
     * @param currentUser The currently authenticated user, injected by Spring Security using the @AuthenticationPrincipal annotation. This user is used to ensure that the post image is generated for the correct user and to update the post with the generated image if the user is the author of the post.
     * @param id The ObjectId of the post for which the image is to be generated. This path variable is used to identify the specific post that the user wants to generate an image for, and it is passed to the GenerationService to ensure that the correct post is updated with the generated image.
     * @return A ResponseEntity containing a MessageResponseDTO with the result of the operation. If the user is not authenticated, it returns a 401 Unauthorized response. If the generation is successful, it returns a 200 OK response with a success message.
     *         If there is an error during generation (e.g., invalid prompt or user is not the author of the post), it returns a 400 Bad Request response with the error message.
     */
    @PostMapping("/post-image/{id}")
    @Operation(summary = "Generate a post image based on a user-provided prompt")
    @ApiResponse(responseCode = "200", description = "Post image generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid prompt, user is not the author of the post, or error during generation")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<MessageResponseDTO> generatePostImage(@RequestBody GenerationRequestDTO request,
                                           @AuthenticationPrincipal User currentUser,
                                           @PathVariable ObjectId id) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDTO("Unauthorized"));
        }
        try {
            generationService.generatePostImage(currentUser, id, request.prompt());
            return ResponseEntity.ok(new MessageResponseDTO("Post image generated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDTO(e.getMessage()));
        }
    }
}
