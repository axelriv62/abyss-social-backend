package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.model.Group;
import fr.univartois.butinfo.sae.abyss.social.model.Page;
import fr.univartois.butinfo.sae.abyss.social.model.Post;
import fr.univartois.butinfo.sae.abyss.social.model.ROLES;
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

    /** Repository used to validate and update target groups for post attachment. */
    private GroupRepository groupRepository;

    /** Repository used to validate and update target pages for post attachment. */
    private PageRepository pageRepository;

    /**
     * Builds the service with required repositories.
     *
     * @param postRepository repository handling Post entities
     * @param userRepository repository handling User entities
     * @param groupRepository repository handling Group entities
     * @param pageRepository repository handling Page entities
     */
    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       GroupRepository groupRepository,
                       PageRepository pageRepository) {
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
        if (post.getCreatedAt() == null) {
            post.setCreatedAt(LocalDateTime.now());
        }
        return postRepository.save(post);
    }

    /**
     * Creates a post and attaches it to the provided group.
     *
     * @param post post to create
     * @param groupId target group identifier
     * @return persisted post
     */
    public Post saveInGroup(Post post, ObjectId groupId) {
        Group group = getGroupOrThrow(groupId);
        ObjectId authorId = extractPostAuthorId(post);
        User author = getUserOrThrow(authorId);

        if (!isGroupCreator(group, authorId) && !isGroupMember(author, groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the group creator or group members can publish posts in this group");
        }

        Post savedPost = save(post);
        group.setPosts(appendPostId(group.getPosts(), savedPost.getId()));
        groupRepository.save(group);
        return savedPost;
    }

    /**
     * Creates a post and attaches it to the provided page.
     *
     * @param post post to create
     * @param pageId target page identifier
     * @return persisted post
     */
    public Post saveInPage(Post post, ObjectId pageId) {
        Page page = getPageOrThrow(pageId);
        ObjectId authorId = extractPostAuthorId(post);

        if (!isPageCreator(page, authorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the page creator can publish posts on this page");
        }

        Post savedPost = save(post);
        page.setPosts(appendPostId(page.getPosts(), savedPost.getId()));
        pageRepository.save(page);
        return savedPost;
    }

    /**
     * Deletes a post if it exists, otherwise raises 404.
     *
     * @param id identifier of the post to delete
     */
    public void deleteById(ObjectId id, User requester) {
        Post post = getPostOrThrow(id);
        if (!canManagePost(post, requester)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator or an admin can delete this post");
        }
        postRepository.deleteById(id);
    }

    /**
     * Retrieves a post by identifier.
     *
     * @param postId identifier of the post to retrieve
     * @return the matching post
     */
    public Post findByIdOrThrow(ObjectId postId) {
        return getPostOrThrow(postId);
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
     * Retrieves a paginated feed for a user.
     * Returns posts from: friends, groups, pages, and own posts.
     * Posts are sorted by creation date (most recent first) and deduplicated.
     *
     * @param user the user for whom to retrieve the feed
     * @param offset starting position in the sorted list (must be >= 0)
     * @param limit maximum number of posts to return (must be > 0 and <= 100)
     * @return list of Post objects representing the user's paginated feed
     */
    public List<Post> findAllForUser(User user, int offset, int limit) {
        validateUser(user);
        validatePaginationParameters(offset, limit);
        ensureUserExists(user.getId());

        List<Post> allPosts = collectAllFeedPosts(user);
        return sortAndPaginatePosts(allPosts, offset, limit);
    }

    /**
     * Validates that the user object is not null and has a valid ID.
     *
     * @param user the user to validate
     * @throws ResponseStatusException if user is null or has no ID
     */
    private void validateUser(User user) {
        if (user == null || user.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is required");
        }
    }

    /**
     * Validates pagination parameters (offset and limit).
     *
     * @param offset starting position (must be >= 0)
     * @param limit number of posts to return (must be > 0 and <= 100)
     * @throws ResponseStatusException if parameters are invalid
     */
    private void validatePaginationParameters(int offset, int limit) {
        if (offset < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "offset cannot be negative");
        }
        if (limit <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be greater than 0");
        }
        if (limit > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit cannot exceed 100");
        }
    }

    /**
     * Collects all feed posts for a user from various sources.
     * Combines posts from: friends, groups, pages, and the user's own posts.
     *
     * @param user the user for whom to collect feed posts
     * @return list of all posts before sorting and pagination
     */
    private List<Post> collectAllFeedPosts(User user) {
        List<Post> allPosts = new ArrayList<>();

        addFriendsPosts(user, allPosts);
        addGroupsPosts(user, allPosts);
        addPagesPosts(user, allPosts);
        addUserOwnPosts(user, allPosts);

        return allPosts;
    }

    /**
     * Adds posts from the user's friends to the feed.
     *
     * @param user the user whose friends' posts to collect
     * @param postsList the list to which posts will be added
     */
    private void addFriendsPosts(User user, List<Post> postsList) {
        if (user.getFriends() != null && !user.getFriends().isEmpty()) {
            postsList.addAll(postRepository.findByUser_IdIn(user.getFriends()));
        }
    }

    /**
     * Adds posts from the user's joined groups to the feed.
     *
     * @param user the user whose groups' posts to collect
     * @param postsList the list to which posts will be added
     */
    private void addGroupsPosts(User user, List<Post> postsList) {
        if (user.getGroups() != null && !user.getGroups().isEmpty()) {
            List<Group> userGroups = groupRepository.findAllById(user.getGroups());
            for (Group group : userGroups) {
                if (group.getPosts() != null && group.getPosts().length > 0) {
                    postsList.addAll(postRepository.findAllById(Arrays.asList(group.getPosts())));
                }
            }
        }
    }

    /**
     * Adds posts from the user's followed pages to the feed.
     *
     * @param user the user whose pages' posts to collect
     * @param postsList the list to which posts will be added
     */
    private void addPagesPosts(User user, List<Post> postsList) {
        if (user.getPages() != null && !user.getPages().isEmpty()) {
            List<Page> userPages = pageRepository.findAllById(user.getPages());
            for (Page page : userPages) {
                if (page.getPosts() != null && page.getPosts().length > 0) {
                    postsList.addAll(postRepository.findAllById(Arrays.asList(page.getPosts())));
                }
            }
        }
    }

    /**
     * Adds the user's own posts to the feed.
     *
     * @param user the user whose own posts to collect
     * @param postsList the list to which posts will be added
     */
    private void addUserOwnPosts(User user, List<Post> postsList) {
        postsList.addAll(postRepository.findByUser_Id(user.getId()));
    }

    /**
     * Sorts and paginates a list of posts.
     * Removes duplicates, sorts by creation date (most recent first),
     * then applies offset and limit.
     *
     * @param posts the list of posts to sort and paginate
     * @param offset starting position in the sorted list
     * @param limit number of posts to return
     * @return paginated and sorted list of posts
     */
    private List<Post> sortAndPaginatePosts(List<Post> posts, int offset, int limit) {
        return posts.stream()
                .distinct()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .skip(offset)
                .limit(limit)
                .toList();
    }

    /**
     * Retrieves a post or throws 404 if not found.
     *
     * @param postId the ID of the post to retrieve
     * @return the post if found
     * @throws ResponseStatusException if post is not found
     */
    private Post getPostOrThrow(ObjectId postId) {
        if (postId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "postId is required");
        }
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found for id=" + postId.toHexString()));
    }

    /**
     * Retrieves a group or throws 404 if not found.
     *
     * @param groupId the ID of the group to retrieve
     * @return the group if found
     * @throws ResponseStatusException if group is not found
     */
    private Group getGroupOrThrow(ObjectId groupId) {
        if (groupId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "groupId is required");
        }
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found for id=" + groupId.toHexString()));
    }

    /**
     * Retrieves a page or throws 404 if not found.
     *
     * @param pageId the ID of the page to retrieve
     * @return the page if found
     * @throws ResponseStatusException if page is not found
     */
    private Page getPageOrThrow(ObjectId pageId) {
        if (pageId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pageId is required");
        }
        return pageRepository.findById(pageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found for id=" + pageId.toHexString()));
    }

    /**
     * Retrieves a user or throws 404 if not found.
     *
     * @param userId the ID of the user to retrieve
     * @return the user if found
     * @throws ResponseStatusException if user is not found
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
     *
     * @param userId the ID of the user to check
     * @throws ResponseStatusException if user does not exist
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
     *
     * @param users the list from which to remove the user
     * @param userId the ID of the user to remove
     */
    private void removeUserFromList(List<User> users, ObjectId userId) {
        users.removeIf(existing -> isSameUser(existing, userId));
    }

    /**
     * Checks whether a user with the given identifier is already present in the list.
     *
     * @param users the list to search
     * @param userId the ID of the user to find
     * @return true if the user is in the list, false otherwise
     */
    private boolean containsUser(List<User> users, ObjectId userId) {
        return users.stream().anyMatch(existing -> isSameUser(existing, userId));
    }

    /**
     * Compares a user instance with an identifier, handling nulls safely.
     *
     * @param user the user instance to compare
     * @param userId the ID to compare against
     * @return true if the user's ID matches the provided ID, false otherwise
     */
    private boolean isSameUser(User user, ObjectId userId) {
        return user != null && user.getId() != null && user.getId().equals(userId);
    }

    /**
     * Extracts and validates the author identifier from a post payload.
     */
    private ObjectId extractPostAuthorId(Post post) {
        if (post == null || post.getUser() == null || post.getUser().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        return post.getUser().getId();
    }

    /**
     * Checks whether the user is the creator of the provided page.
     */
    private boolean isPageCreator(Page page, ObjectId userId) {
        return page != null
                && page.getUser() != null
                && page.getUser().getId() != null
                && page.getUser().getId().equals(userId);
    }

    /**
     * Checks whether the user is the creator of the provided group.
     */
    private boolean isGroupCreator(Group group, ObjectId userId) {
        if (group == null) {
            return false;
        }
        return group.getUser() != null
                && group.getUser().getId() != null
                && group.getUser().getId().equals(userId);
    }

    /**
     * Checks whether the user is member of the provided group.
     */
    private boolean isGroupMember(User user, ObjectId groupId) {
        return user != null
                && user.getGroups() != null
                && user.getGroups().contains(groupId);
    }

    /**
     * Checks whether the requester is allowed to delete the post.
     */
    private boolean canManagePost(Post post, User requester) {
        if (requester == null || requester.getId() == null || post == null || post.getUser() == null || post.getUser().getId() == null) {
            return false;
        }
        return requester.getId().equals(post.getUser().getId()) || requester.getRole() == ROLES.ADMIN;
    }

    /**
     * Appends an identifier to an ObjectId array if it is not already present.
     *
     * @param existingPostIds the original array of post IDs
     * @param postId the ID to append
     * @return updated array with the new ID if not already present
     */
    private ObjectId[] appendPostId(ObjectId[] existingPostIds, ObjectId postId) {
        ObjectId[] source = existingPostIds == null ? new ObjectId[0] : existingPostIds;
        boolean alreadyPresent = Arrays.stream(source).anyMatch(postId::equals);
        if (alreadyPresent) {
            return source;
        }
        ObjectId[] updated = Arrays.copyOf(source, source.length + 1);
        updated[source.length] = postId;
        return updated;
    }
}
