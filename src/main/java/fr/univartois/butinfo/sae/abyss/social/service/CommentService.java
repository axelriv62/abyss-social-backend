package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.model.Comment;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.repository.CommentRepository;
import fr.univartois.butinfo.sae.abyss.social.repository.PostRepository;
import fr.univartois.butinfo.sae.abyss.social.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for comment lifecycle operations.
 * It validates references (post, user), enforces ownership rules,
 * and delegates persistence to repositories.
 */
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * Creates a comment service with required repositories.
     *
     * @param commentRepository repository used to persist and load comments
     * @param postRepository repository used to validate related posts
     * @param userRepository repository used to validate and load users
     */
    public CommentService(CommentRepository commentRepository,
                          PostRepository postRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * Saves a new comment after validating related post, author, and content.
     * If the creation timestamp is missing, it is set to the current time.
     *
     * @param comment comment to validate and persist
     * @return saved comment
     */
    public Comment save(Comment comment) {
        ObjectId postId = comment.getPostId();
        if (postId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "postId is required");
        }
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found for id=" + postId.toHexString());
        }
        User author = comment.getUser();
        ObjectId userId;
        if (author != null) {
            userId = author.getId();
        } else {
            userId = null;
        }
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        User persistedUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for userId=" + userId.toHexString()));
        if (comment.getText() == null || comment.getText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "text cannot be blank");
        }
        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(LocalDateTime.now());
        }
        comment.setUser(persistedUser);
        return commentRepository.save(comment);
    }

    /**
     * Returns all comments linked to a given post.
     *
     * @param postId identifier of the target post
     * @return comments attached to the post
     */
    public List<Comment> findByPostId(ObjectId postId) {
        if (postId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "postId is required");
        }
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found for id=" + postId.toHexString());
        }
        return commentRepository.findByPostId(postId);
    }

    /**
     * Deletes a comment only if the requester is its author.
     *
     * @param commentId identifier of the comment to delete
     * @param requesterId identifier of the authenticated user requesting deletion
     */
    public void deleteComment(ObjectId commentId, ObjectId requesterId) {
        if (commentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "commentId is required");
        }
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found for id=" + commentId.toHexString()));
        if (comment.getUser() == null || comment.getUser().getId() == null
                || !comment.getUser().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can delete this comment");
        }
        commentRepository.deleteById(commentId);
    }

    /**
     * Updates the text of a comment if the requester is the comment author.
     * The creation timestamp is also refreshed to the current time.
     *
     * @param commentId identifier of the comment to update
     * @param requesterId identifier of the authenticated user
     * @param text new comment content
     * @return updated comment
     */
    public Comment updateComment(ObjectId commentId, ObjectId requesterId, String text) {
        if (commentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "commentId is required");
        }
        if (text == null || text.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "text cannot be blank");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found for id=" + commentId.toHexString()));

        if (comment.getUser() == null || comment.getUser().getId() == null
                || !comment.getUser().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can update this comment");
        }

        comment.setText(text.trim());
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }
}
