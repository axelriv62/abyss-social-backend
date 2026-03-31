package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.model.Post;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.repository.PostRepository;
import fr.univartois.butinfo.sae.abyss.social.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PostService {

    private PostRepository postRepository;
    private UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

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

    public void deleteById(ObjectId id) {
        if (!postRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        postRepository.deleteById(id);
    }

    public Post likePost(ObjectId postId, ObjectId userId) {
        Post post = getPostOrThrow(postId);
        User user = getUserOrThrow(userId);
        removeUserFromList(post.getDislikes(), userId);
        if (!containsUser(post.getLikes(), userId)) {
            post.getLikes().add(user);
        }
        return postRepository.save(post);
    }

    public Post unlikePost(ObjectId postId, ObjectId userId) {
        Post post = getPostOrThrow(postId);
        ensureUserExists(userId);
        removeUserFromList(post.getLikes(), userId);
        return postRepository.save(post);
    }

    public Post dislikePost(ObjectId postId, ObjectId userId) {
        Post post = getPostOrThrow(postId);
        User user = getUserOrThrow(userId);
        removeUserFromList(post.getLikes(), userId);
        if (!containsUser(post.getDislikes(), userId)) {
            post.getDislikes().add(user);
        }
        return postRepository.save(post);
    }

    public Post undislikePost(ObjectId postId, ObjectId userId) {
        Post post = getPostOrThrow(postId);
        ensureUserExists(userId);
        removeUserFromList(post.getDislikes(), userId);
        return postRepository.save(post);
    }

    private Post getPostOrThrow(ObjectId postId) {
        if (postId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "postId is required");
        }
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found for id=" + postId.toHexString()));
    }

    private User getUserOrThrow(ObjectId userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for userId=" + userId.toHexString()));
    }

    private void ensureUserExists(ObjectId userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for userId=" + userId.toHexString());
        }
    }

    private void removeUserFromList(List<User> users, ObjectId userId) {
        users.removeIf(existing -> isSameUser(existing, userId));
    }

    private boolean containsUser(List<User> users, ObjectId userId) {
        return users.stream().anyMatch(existing -> isSameUser(existing, userId));
    }

    private boolean isSameUser(User user, ObjectId userId) {
        return user != null && user.getId() != null && user.getId().equals(userId);
    }
}
