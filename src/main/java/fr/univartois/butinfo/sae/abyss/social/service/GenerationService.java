package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.model.Post;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.repository.PostRepository;
import fr.univartois.butinfo.sae.abyss.social.repository.UserRepository;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Service class responsible for handling image generation requests. It uses a RestClient to communicate with an external image generation service, sending prompts and receiving generated images in response. The service provides methods for generating profile pictures and post images, ensuring that only authorized users can generate images for their own posts.
 * The generate method sends a prompt to the external service and retrieves the generated image as a Base64-encoded string, which is then decoded into a Binary object. The generateProfilePicture method updates the user's profile picture with the generated image, while the generatePostImage method updates a post's image, ensuring that the current user is the author of the post before allowing the update.
 * Both methods save the updated entities back to their respective repositories after modification.
 */
@Service
public class GenerationService {

    /**
     * RestClient used to communicate with the external image generation service. It is initialized with the base URL of the service, which is injected from the application properties using the @Value annotation.
     * This client will be used to send HTTP requests to the image generation service and receive responses containing the generated images.
     */
    private final RestClient restClient;

    /**
     * UserRepository used to access and modify user data in the database. It is injected through the constructor and allows the service to update user profile pictures after generating new images.
     */
    private final UserRepository userRepository;

    /**
     * PostRepository used to access and modify post data in the database. It is injected through the constructor and allows the service to update post images after generating new images, ensuring that only the author of the post can update its image.
     */
    private final PostRepository postRepository;

    /**
     * Constructor for the GenerationService class. It initializes the RestClient with the base URL of the external image generation service and sets up the UserRepository and PostRepository for database access. The base URL is injected from the application properties, allowing for flexible configuration of the service endpoint.
     * @param baseUrl The base URL of the external image generation service, injected from the application properties using the @Value annotation. This URL is used to configure the RestClient for sending requests to the image generation service.
     * @param userRepository The UserRepository used to access and modify user data in the database. This repository allows the service to update user profile pictures after generating new images.
     * @param postRepository The PostRepository used to access and modify post data in the database. This repository allows the service to update post images after generating new images, ensuring that only the author of the post can update its image.
     */
    public GenerationService(@Value("${stable-diffusion.api.base-url}") String baseUrl, UserRepository userRepository, PostRepository postRepository) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    /**
     * Generates an image based on the provided prompt by sending a request to the external image generation service.
     * The method constructs a request body containing the prompt, sends it to the "/generate" endpoint of the service, and retrieves the response, which is expected to contain a Base64-encoded image string. The method then decodes this string into a Binary object and returns it.
     * If the response is invalid or does not contain the expected "image" key, an IllegalArgumentException is thrown.
     * @param prompt The textual prompt used to generate the image. This prompt is sent to the external image generation service, which processes it and returns a generated image based on the content of the prompt.
     * @return A Binary object containing the generated image data. This object is created by decoding the Base64-encoded image string received from the external service, allowing it to be stored and used within the application as needed.
     */
    public Binary generate(String prompt) {
        Map<String, String> body = Map.of("prompt", prompt);
        Map<String, String> response = restClient.post()
                .uri("/generate")
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (response == null || !response.containsKey("image")) {
            throw new IllegalArgumentException("Invalid response from image generation service");
        }

        String base64Image = response.get("image");
        return new Binary(java.util.Base64.getDecoder().decode(base64Image));
    }

    /**
     * Generates a profile picture for the given user based on the provided prompt.
     * The method calls the generate method to create an image from the prompt, then updates the user's profile picture with the generated image and saves the updated user entity back to the database using the UserRepository.
     * @param user The User entity for whom the profile picture is being generated. This user will have their profile picture updated with the generated image after the generation process is complete.
     * @param prompt The textual prompt used to generate the profile picture. This prompt is sent to the external image generation service, which processes it and returns a generated image based on the content of the prompt. The generated image is then set as the user's profile picture and saved to the database.
     */
    public void generateProfilePicture(User user, String prompt) {
        Binary result = generate(prompt);
        user.setProfilePicture(result);
        userRepository.save(user);
    }

    public void generatePostImage(User currentUser, ObjectId postId, String prompt) {
        Binary result = generate(prompt);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (post.getUser() == null || !post.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You can only generate an image for your own posts");
        }

        post.setImage(result);
        postRepository.save(post);
    }
}