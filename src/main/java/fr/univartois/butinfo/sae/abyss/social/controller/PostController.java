package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.CommentDTO;
import fr.univartois.butinfo.sae.abyss.social.dto.PostDTO;
import fr.univartois.butinfo.sae.abyss.social.dto.PostWithCommentsDTO;
import fr.univartois.butinfo.sae.abyss.social.mapper.CommentMapper;
import fr.univartois.butinfo.sae.abyss.social.mapper.PostMapper;
import fr.univartois.butinfo.sae.abyss.social.model.Comment;
import fr.univartois.butinfo.sae.abyss.social.model.Post;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.service.CommentService;
import fr.univartois.butinfo.sae.abyss.social.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    /** Mapper converting between Post entities and DTOs. */
    private final PostMapper postMapper;
    /** Service handling comment retrieval for post details. */
    private final CommentService commentService;
    /** Mapper converting between Comment entities and DTOs. */
    private final CommentMapper commentMapper;

    /**
     * Builds the controller with required collaborators.
     *
     * @param postService service managing posts
     * @param postMapper mapper converting Post ↔ PostDTO
     */
    public PostController(PostService postService,
                          PostMapper postMapper,
                          CommentService commentService,
                          CommentMapper commentMapper) {
        this.postService = postService;
        this.postMapper = postMapper;
        this.commentService = commentService;
        this.commentMapper = commentMapper;
    }

    /**
     * Retrieves a post with all its comments.
     *
     * @param postId identifier of the post
     * @param currentUser authenticated user
     * @return post details with comments
     */
    @Operation(summary = "Get post details", description = "Retrieves a post with its comments.")
    @ApiResponse(responseCode = "200", description = "Post details retrieved")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Post not found")
    @GetMapping("/{postId}")
    public ResponseEntity<PostWithCommentsDTO> getPostWithComments(@PathVariable ObjectId postId,
                                                                   @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Post post = postService.findByIdOrThrow(postId);
        List<Comment> comments = commentService.findByPostId(postId);
        PostDTO postDTO = postMapper.toDTO(post);
        List<CommentDTO> commentDTOs = commentMapper.toDTOs(comments);
        return ResponseEntity.ok(new PostWithCommentsDTO(postDTO, commentDTOs));
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDTO> createPost(
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile image,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Binary imageBinary = null;
        String imageContentType = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageBinary = new Binary(image.getBytes());
                imageContentType = image.getContentType();
            } catch (IOException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        Post post = new Post();
        post.setContent(content);
        post.setImage(imageBinary);
        post.setImageContentType(imageContentType);
        post.setUser(currentUser);
        Post savedPost = postService.save(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(postMapper.toDTO(savedPost));
    }

    /**
     * Creates a post and attaches it to a group.
     *
     * @param groupId target group identifier
     * @param postDTO payload describing the post to create
     * @param currentUser authenticated user creating the post
     * @return created post DTO
     */
    @Operation(summary = "Create a post in a group", description = "Create a new post and attach it to the specified group.")
    @ApiResponse(responseCode = "201", description = "Post successfully created in group")
    @ApiResponse(responseCode = "400", description = "Invalid data")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Group or user not found")
    @PostMapping(value = "/groups/{groupId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDTO> createPostInGroup(@PathVariable ObjectId groupId,
                                                     @RequestParam String content,
                                                     @RequestParam(required = false) MultipartFile image,
                                                     @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Binary imageBinary = null;
        String imageContentType = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageBinary = new Binary(image.getBytes());
                imageContentType = image.getContentType();
            } catch (IOException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        Post post = new Post();
        post.setContent(content);
        post.setImage(imageBinary);
        post.setImageContentType(imageContentType);
        post.setUser(currentUser);
        Post savedPost = postService.saveInGroup(post, groupId);
        return ResponseEntity.status(HttpStatus.CREATED).body(postMapper.toDTO(savedPost));
    }

    /**
     * Creates a post and attaches it to a page.
     *
     * @param pageId target page identifier
     * @param postDTO payload describing the post to create
     * @param currentUser authenticated user creating the post
     * @return created post DTO
     */
    @Operation(summary = "Create a post in a page", description = "Create a new post and attach it to the specified page.")
    @ApiResponse(responseCode = "201", description = "Post successfully created in page")
    @ApiResponse(responseCode = "400", description = "Invalid data")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Page or user not found")
    @PostMapping(value = "/pages/{pageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDTO> createPostInPage(@PathVariable ObjectId pageId,
                                                    @RequestParam String content,
                                                    @RequestParam(required = false) MultipartFile image,
                                                    @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Binary imageBinary = null;
        String imageContentType = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageBinary = new Binary(image.getBytes());
                imageContentType = image.getContentType();
            } catch (IOException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        Post post = new Post();
        post.setContent(content);
        post.setImage(imageBinary);
        post.setImageContentType(imageContentType);
        post.setUser(currentUser);
        Post savedPost = postService.saveInPage(post, pageId);
        return ResponseEntity.status(HttpStatus.CREATED).body(postMapper.toDTO(savedPost));
    }

    /**
     * Deletes an existing post using its identifier.
     *
     * @param postId identifier of the post to delete
     * @return empty response when deletion succeeds
     */
    @Operation(summary = "Delete a post", description = "Delete a post with the specified ID")
    @ApiResponse(responseCode = "204", description = "Post successfully deleted")
    @ApiResponse(responseCode = "400", description = "Post don't exist")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteById(@PathVariable ObjectId postId, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        postService.deleteById(postId, currentUser);
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
     * Retrieves the paginated feed for the authenticated user.
     * Returns posts from the user's friends, groups, pages, and their own posts.
     * Posts are sorted by creation date (most recent first).
     *
     * @param currentUser The authenticated user, injected by Spring Security
     * @param offset Starting position in the feed (default: 0)
     * @param limit Number of posts to return (default: 50, max: 100)
     * @return ResponseEntity containing list of PostDTOs representing the paginated feed
     */
    @GetMapping("/feed")
    @Operation(summary = "Get user's paginated feed", description = "Retrieves posts from friends, groups, pages and own posts with pagination")
    @ApiResponse(responseCode = "200", description = "Feed retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<List<PostDTO>> getUserFeed(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Post> posts = postService.findAllForUser(currentUser, offset, limit);
        List<PostDTO> postDTOs = posts.stream()
                .map(postMapper::toDTO)
                .toList();

        return ResponseEntity.ok(postDTOs);
    }

}
