package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.CommentDTO;
import fr.univartois.butinfo.sae.abyss.social.dto.CommentPatchDTO;
import fr.univartois.butinfo.sae.abyss.social.mapper.CommentMapper;
import fr.univartois.butinfo.sae.abyss.social.model.Comment;
import fr.univartois.butinfo.sae.abyss.social.model.Post;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    public CommentController(CommentService commentService, CommentMapper commentMapper) {
        this.commentService = commentService;
        this.commentMapper = commentMapper;
    }

    @Operation(summary = "Create a comment", description = "Adds a new comment on the targeted post.")
    @ApiResponse(responseCode = "201", description = "Comment created")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Post or user not found")
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentDTO> createComment(@PathVariable ObjectId postId,
                                                    @Valid @RequestBody CommentDTO request,
                                                    @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Comment comment = commentMapper.toEntity(request);
        Post post = new Post();
        post.setId(postId);
        comment.setPost(post);
        comment.setUser(currentUser);
        Comment saved = commentService.save(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentMapper.toDTO(saved));
    }

    @Operation(summary = "List comments", description = "Lists every comment attached to the provided post.")
    @ApiResponse(responseCode = "200", description = "Comments retrieved")
    @ApiResponse(responseCode = "400", description = "Invalid post identifier")
    @ApiResponse(responseCode = "404", description = "Post not found")
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentDTO>> listComments(@PathVariable ObjectId postId) {
        List<Comment> comments = commentService.findByPostId(postId);
        return ResponseEntity.ok(commentMapper.toDTOs(comments));
    }

    @Operation(summary = "Delete a comment", description = "Deletes the targeted comment if the authenticated user is the author.")
    @ApiResponse(responseCode = "204", description = "Comment deleted")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Deletion forbidden")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable ObjectId commentId,
                                              @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        commentService.deleteComment(commentId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a comment", description = "Updates the targeted comment text if the authenticated user is the author.")
    @ApiResponse(responseCode = "200", description = "Comment updated")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Update forbidden")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable ObjectId commentId,
                                                    @Valid @RequestBody CommentPatchDTO request,
                                                    @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Comment updated = commentService.updateComment(commentId, currentUser, request.text());
        return ResponseEntity.ok(commentMapper.toDTO(updated));
    }
}
