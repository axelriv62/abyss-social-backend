package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.PostDTO;
import fr.univartois.butinfo.sae.abyss.social.mapper.PostMapper;
import fr.univartois.butinfo.sae.abyss.social.model.Post;
import fr.univartois.butinfo.sae.abyss.social.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing CRUD and reaction endpoints for posts.
 */
@RestController
@RequestMapping("/posts")
public class PostController {
    /** Service handling business logic for posts. */
    private final PostService postService;
    /** Mapper converting between Post entities and DTOs. */
    private final PostMapper postMapper;

    /**
     * Builds the controller with required collaborators.
     *
     * @param postService service managing posts
     * @param postMapper mapper converting Post ↔ PostDTO
     */
    public PostController(PostService postService, PostMapper postMapper) {
        this.postService = postService;
        this.postMapper = postMapper;
    }

    /**
     * Creates a post from the provided payload.
     *
     * @param postDTO payload describing the post to create
     * @return the persisted post DTO
     */
    @Operation(summary = "Create a new post", description = "Create a new post with the provided data")
    @ApiResponse(responseCode = "200", description = "Post successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid data")
    @ApiResponse(responseCode = "404", description = "User don't exist")
    @PostMapping
    public ResponseEntity<PostDTO> createPost(@Valid @RequestBody PostDTO postDTO) {
        Post post = postMapper.toEntity(postDTO);
        Post savedPost = postService.save(post);

        return ResponseEntity.status(HttpStatus.CREATED).body(postMapper.toDTO(savedPost));

    }

    /**
     * Deletes an existing post using its identifier.
     *
     * @param id identifier of the post to delete
     * @return empty response when deletion succeeds
     */
    @Operation(summary = "Delete a post", description = "Delete a post with the specified ID")
    @ApiResponse(responseCode = "204", description = "Post successfully deleted")
    @ApiResponse(responseCode = "400", description = "Post don't exist")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovieById(@PathVariable ObjectId id) {
        postService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Registers a like from the provided user on the selected post.
     */
    @Operation(summary = "Like a post", description = "Registers a like from the provided user on the selected post.")
    @ApiResponse(responseCode = "200", description = "Post liked")
    @ApiResponse(responseCode = "400", description = "Invalid identifiers")
    @ApiResponse(responseCode = "404", description = "Post or user not found")
    @PostMapping("/{postId}/likes")
    public ResponseEntity<PostDTO> likePost(@PathVariable ObjectId postId, @RequestParam ObjectId userId) {
        Post updated = postService.likePost(postId, userId);
        return ResponseEntity.ok(postMapper.toDTO(updated));
    }

    /**
     * Removes the like previously set by the user on this post.
     */
    @Operation(summary = "Remove a like", description = "Removes the like previously set by the provided user on this post.")
    @ApiResponse(responseCode = "200", description = "Like removed")
    @ApiResponse(responseCode = "400", description = "Invalid identifiers")
    @ApiResponse(responseCode = "404", description = "Post or user not found")
    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<PostDTO> unlikePost(@PathVariable ObjectId postId, @RequestParam ObjectId userId) {
        Post updated = postService.unlikePost(postId, userId);
        return ResponseEntity.ok(postMapper.toDTO(updated));
    }

    /**
     * Registers a dislike from the provided user on the selected post.
     */
    @Operation(summary = "Dislike a post", description = "Registers a dislike from the provided user on the selected post.")
    @ApiResponse(responseCode = "200", description = "Post disliked")
    @ApiResponse(responseCode = "400", description = "Invalid identifiers")
    @ApiResponse(responseCode = "404", description = "Post or user not found")
    @PostMapping("/{postId}/dislikes")
    public ResponseEntity<PostDTO> dislikePost(@PathVariable ObjectId postId, @RequestParam ObjectId userId) {
        Post updated = postService.dislikePost(postId, userId);
        return ResponseEntity.ok(postMapper.toDTO(updated));
    }

    /**
     * Removes the dislike previously set by the user on this post.
     */
    @Operation(summary = "Remove a dislike", description = "Removes the dislike previously set by the provided user on this post.")
    @ApiResponse(responseCode = "200", description = "Dislike removed")
    @ApiResponse(responseCode = "400", description = "Invalid identifiers")
    @ApiResponse(responseCode = "404", description = "Post or user not found")
    @DeleteMapping("/{postId}/dislikes")
    public ResponseEntity<PostDTO> undislikePost(@PathVariable ObjectId postId, @RequestParam ObjectId userId) {
        Post updated = postService.undislikePost(postId, userId);
        return ResponseEntity.ok(postMapper.toDTO(updated));
    }
}
