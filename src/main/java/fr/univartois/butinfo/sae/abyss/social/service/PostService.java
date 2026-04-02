package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.model.Group;
import fr.univartois.butinfo.sae.abyss.social.model.Page;
import fr.univartois.butinfo.sae.abyss.social.model.Post;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.repository.GroupRepository;
import fr.univartois.butinfo.sae.abyss.social.repository.PageRepository;
import fr.univartois.butinfo.sae.abyss.social.repository.PostRepository;
import fr.univartois.butinfo.sae.abyss.social.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
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

    private GroupRepository groupRepository;

    private PageRepository pageRepository;

    /**
     * Builds the service with required repositories.
     *
     * @param postRepository repository handling Post entities
     * @param userRepository repository handling User entities
     */
    public PostService(PostRepository postRepository, UserRepository userRepository, GroupRepository groupRepository, PageRepository pageRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.pageRepository = pageRepository;
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
        if (date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date is required");
        }
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return postRepository.findByCreatedAtBetween(startOfDay, endOfDay);
    }

    /**
     * Finds posts authored by the specified username, case-insensitively.
     *
     * @param username creator username fragment to match exactly
     * @return posts authored by the given username
     */
    public List<Post> searchByCreatorUsername(String username) {
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username is required");
        }
        String trimmed = username.trim();
        if (trimmed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username cannot be blank");
        }
        User creator = userRepository.findByUsernameIgnoreCase(trimmed)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for username=" + trimmed));
        return postRepository.findByUser_Id(creator.getId());
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

    /**
     * Retrieves the complete feed for a user.
     * Returns posts from: friends, groups, pages, and own posts.
     * Posts are sorted by creation date (most recent first) and deduplicated.
     */
    public List<Post> findAllForUser(User user) {
        if (user == null || user.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is required");
        }

        // Check if user exists
        if (!userRepository.existsById(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        List<Post> posts = new ArrayList<>();

        // 1. Get friends' posts (friends are stored as ObjectId list, posts are linked to users by userId)
        if (user.getFriends() != null && !user.getFriends().isEmpty()) {
            posts.addAll(postRepository.findByUser_IdIn(user.getFriends()));
        }

        // 2. Get groups' posts (groups store ObjectId[] of posts)
        if (user.getGroups() != null && !user.getGroups().isEmpty()) {
            List<Group> userGroups = groupRepository.findAllById(user.getGroups());
            for (Group group : userGroups) {
                if (group.getPosts() != null && group.getPosts().length > 0) {
                    posts.addAll(postRepository.findAllById(Arrays.asList(group.getPosts())));
                }
            }
        }

        // 3. Get pages' posts (pages store ObjectId[] of posts)
        if (user.getPages() != null && !user.getPages().isEmpty()) {
            List<Page> userPages = pageRepository.findAllById(user.getPages());
            for (Page page : userPages) {
                if (page.getPosts() != null && page.getPosts().length > 0) {
                    posts.addAll(postRepository.findAllById(Arrays.asList(page.getPosts())));
                }
            }
        }

        // Sort by date (most recent first) and remove duplicates
        return posts.stream()
                .distinct()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .toList();
    }

}

