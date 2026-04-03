package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.PostDTO;
import fr.univartois.butinfo.sae.abyss.social.mapper.PostMapper;
import fr.univartois.butinfo.sae.abyss.social.model.Post;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.service.PostService;
import fr.univartois.butinfo.sae.abyss.social.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller exposing CRUD and reaction endpoints for posts.
 */
@RestController
@RequestMapping("/posts")
public class PostController {
    /** Service handling business logic for posts and users. */
    private final PostService postService;
    private final UserService userService;
    /** Mapper converting between Post entities and DTOs. */
    private final PostMapper postMapper;

    /**
     * Builds the controller with required collaborators.
     *
     * @param postService service managing posts
     * @param postMapper mapper converting Post ↔ PostDTO
     */
    public PostController(PostService postService, UserService userService, PostMapper postMapper) {
        this.postService = postService;
        this.userService = userService;
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
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @PostMapping
    public ResponseEntity<PostDTO> createPost(@Valid @RequestBody PostDTO postDTO, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Post post = postMapper.toEntity(postDTO);
        post.setUser(currentUser);
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
     * Searches posts containing the provided text fragment in their content.
     *
     * @param query fragment to search for
     * @return list of matching posts
     */
    @Operation(summary = "Search posts", description = "Lists posts whose content contains the provided fragment, case-insensitively.")
    @ApiResponse(responseCode = "200", description = "Search completed")
    @ApiResponse(responseCode = "400", description = "Query is invalid")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping("/search")
    public ResponseEntity<List<PostDTO>> searchPosts(@RequestParam("query") String query, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Post> matches = postService.searchByContent(query);
        return ResponseEntity.ok(postMapper.toDTOs(matches));
    }

    /**
     * Searches posts created on the provided date.
     */
    @Operation(summary = "Search posts by date", description = "Lists posts created on the provided date, ignoring time.")
    @ApiResponse(responseCode = "200", description = "Search completed")
    @ApiResponse(responseCode = "400", description = "Date is invalid")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping("/search/by-date")
    public ResponseEntity<List<PostDTO>> searchPostsByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Post> matches = postService.searchByCreationDate(date);
        return ResponseEntity.ok(postMapper.toDTOs(matches));
    }

    /**
     * Searches posts authored by the provided username.
     */
    @Operation(summary = "Search posts by creator", description = "Lists posts created by the provided username, ignoring case.")
    @ApiResponse(responseCode = "200", description = "Search completed")
    @ApiResponse(responseCode = "400", description = "Username is invalid")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping("/search/by-creator")
    public ResponseEntity<List<PostDTO>> searchPostsByCreator(@RequestParam("username") String username, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Post> matches = postService.searchByCreatorUsername(username);
        return ResponseEntity.ok(postMapper.toDTOs(matches));
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

    /**
     * Retrieves the feed for the authenticated user.
     * Returns all posts from the user's friends, groups, pages, and their own posts.
     * Posts are sorted by creation date (most recent first).
     *
     * @param currentUser The authenticated user, injected by Spring Security
     * @return ResponseEntity containing list of PostDTOs representing the user's feed
     */
    @GetMapping("/feed")
    @Operation(summary = "Get user's feed", description = "Retrieves all posts from friends, groups, pages and own posts")
    @ApiResponse(responseCode = "200", description = "Feed retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<List<PostDTO>> getUserFeed(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Post> posts = postService.findAllForUser(currentUser);
        List<PostDTO> postDTOs = posts.stream()
                .map(postMapper::toDTO)
                .toList();
        return ResponseEntity.ok(postDTOs);
    }
}
