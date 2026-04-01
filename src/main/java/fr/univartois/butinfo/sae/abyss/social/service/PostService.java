package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.model.Post;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.repository.PostRepository;
import fr.univartois.butinfo.sae.abyss.social.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Service exposing CRUD operations and reactions (likes/dislikes) for posts.
 * Validates post and user identifiers before delegating to repositories.
 */
@Service
public class PostService {

    /** Repository managing persistence of posts. */
    private PostRepository postRepository;
    /** Repository used to validate and load users referenced by posts. */
    private UserRepository userRepository;

    /**
     * Builds the service with required repositories.
     *
     * @param postRepository repository handling Post entities
     * @param userRepository repository handling User entities
     */
    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * Persists a post after ensuring the referenced user exists.
     *
     * @param post post to save
     * @return saved instance
     */
    public Post save(Post post) {
        // Extract the user's ObjectId from the post if a user is associated, otherwise leave it as null.
        ObjectId userId;
        if (post.getUser() != null) {
            userId = post.getUser().getId();
        } else {
            userId = null;
        }
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        getUserOrThrow(userId);
        return postRepository.save(post);
    }

    /**
     * Deletes a post if it exists, otherwise raises 404.
     *
     * @param id identifier of the post to delete
     */
    public void deleteById(ObjectId id) {
        if (!postRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        postRepository.deleteById(id);
    }

    /**
     * Finds posts whose content contains the provided fragment, case-insensitively.
     *
     * @param contentFragment substring to search for
     * @return posts matching the fragment
     */
    public List<Post> searchByContent(String contentFragment) {
        String trimmed = contentFragment.trim();
        if (trimmed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "query cannot be blank");
        }
        return postRepository.findByContentContainingIgnoreCase(trimmed);
    }

    /**
     * Finds posts created on the provided date, ignoring the time component.
     *
     * @param date creation date to match
     * @return posts whose creation timestamp falls within that day
     */
    public List<Post> searchByCreationDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return postRepository.findByCreatedAtBetween(startOfDay, endOfDay);
    }

    /**
     * Adds a like for the given user, removing any existing dislike first.
     *
     * @param postId identifier of the post
     * @param userId identifier of the user
     * @return updated post
     */
    public Post likePost(ObjectId postId, ObjectId userId) {
        Post post = getPostOrThrow(postId);
        User user = getUserOrThrow(userId);
        removeUserFromList(post.getDislikes(), userId);
        if (!containsUser(post.getLikes(), userId)) {
            post.getLikes().add(user);
        }
        return postRepository.save(post);
    }

    /**
     * Removes a like previously set by the user.
     *
     * @param postId identifier of the post
     * @param userId identifier of the user
     * @return updated post
     */
    public Post unlikePost(ObjectId postId, ObjectId userId) {
        Post post = getPostOrThrow(postId);
        ensureUserExists(userId);
        removeUserFromList(post.getLikes(), userId);
        return postRepository.save(post);
    }

    /**
     * Adds a dislike for the given user, removing any existing like first.
     *
     * @param postId identifier of the post
     * @param userId identifier of the user
     * @return updated post
     */
    public Post dislikePost(ObjectId postId, ObjectId userId) {
        Post post = getPostOrThrow(postId);
        User user = getUserOrThrow(userId);
        removeUserFromList(post.getLikes(), userId);
        if (!containsUser(post.getDislikes(), userId)) {
            post.getDislikes().add(user);
        }
        return postRepository.save(post);
    }

    /**
     * Removes a dislike previously set by the user.
     *
     * @param postId identifier of the post
     * @param userId identifier of the user
     * @return updated post
     */
    public Post undislikePost(ObjectId postId, ObjectId userId) {
        Post post = getPostOrThrow(postId);
        ensureUserExists(userId);
        removeUserFromList(post.getDislikes(), userId);
        return postRepository.save(post);
    }

    /**
     * Retrieves a post or throws 404 if not found.
     */
    private Post getPostOrThrow(ObjectId postId) {
        if (postId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "postId is required");
        }
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found for id=" + postId.toHexString()));
    }

    /**
     * Retrieves a user or throws 404 if not found.
     */
    private User getUserOrThrow(ObjectId userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for userId=" + userId.toHexString()));
    }

    /**
     * Ensures a user exists without loading the full entity.
     */
    private void ensureUserExists(ObjectId userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for userId=" + userId.toHexString());
        }
    }

    /**
     * Removes the user identified by {@code userId} from the provided list.
     */
    private void removeUserFromList(List<User> users, ObjectId userId) {
        users.removeIf(existing -> isSameUser(existing, userId));
    }

    /**
     * Checks whether a user with the given identifier is already present in the list.
     */
    private boolean containsUser(List<User> users, ObjectId userId) {
        return users.stream().anyMatch(existing -> isSameUser(existing, userId));
    }

    /**
     * Compares a user instance with an identifier, handling nulls safely.
     */
    private boolean isSameUser(User user, ObjectId userId) {
        return user != null && user.getId() != null && user.getId().equals(userId);
    }
}
