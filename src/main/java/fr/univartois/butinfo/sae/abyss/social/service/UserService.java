package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.repository.UserRepository;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

/**
 * Service class for managing User entities, providing business logic for user-related operations.
 */
@Service
public class UserService implements UserDetailsService {

    /**
     * UserRepository instance for performing CRUD operations on User entities. This repository is injected via the constructor.
     */
    private final UserRepository userRepository;

    /**
     * Constructor for UserService, injecting the UserRepository dependency.
     * @param userRepository The UserRepository instance to be used by this service
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Saves a User entity to the database. This method uses the UserRepository to persist the user and returns the saved User object.
     * @param user The User object to be saved
     * @return The saved User object, including any generated fields such as the unique identifier
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Delete a user by their unique identifier. This method uses the UserRepository to delete the user with the specified ID from the database.
     * @param id The unique identifier of the user to be deleted
     */
    public void deleteById(ObjectId id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    /**
     * Updates a user's profile information (username and profile picture).
     * This method safely updates only non-sensitive user data.
     * Verifies that the username is not already taken by another user.
     *
     * @param userId The ID of the user to update
     * @param username The new username (can be null)
     * @param profilePicture The new profile picture (can be null)
     * @throws ResponseStatusException if username is already in use or user not found
     */
    public void updateProfile(ObjectId userId, String username, Binary profilePicture) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.getUsername().equals(username) && userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already in use");
        }

        user.setUsername(username);
        user.setProfilePicture(profilePicture);
        userRepository.save(user);
    }

    /**
     * Searches for users whose usernames contain the specified fragment, ignoring case.
     * This method trims the input string to remove leading and trailing whitespace, checks if the resulting string is empty, and if not, uses the UserRepository to find and return a list of users whose usernames contain the specified fragment, ignoring case.
     * @param usernameFragment The fragment of the username to search for. This string is trimmed and validated to ensure it is not blank before performing the search.
     * @return A list of User objects whose usernames contain the specified fragment, ignoring case. If the input string is blank after trimming, a ResponseStatusException with a 400 Bad Request status is thrown.
     */
    public List<User> searchByUsername(String usernameFragment) {
        String trimmed = usernameFragment.trim();
        if (trimmed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username fragment cannot be blank");
        }
        return userRepository.findByUsernameContainingIgnoreCase(trimmed);
    }

    /**
     * Loads a user by their username (in this case, email) for authentication purposes. This method is required by the UserDetailsService interface and is used by Spring Security to retrieve user details during the authentication process.
     * @param username the username identifying the user whose data is required (in this implementation, the email is used as the username)
     * @return
     */
    @Override
    public UserDetails loadUserByUsername(@NonNull String username) {
        Optional<User> userOptional = userRepository.findByEmail(username);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email");
        }
        return userOptional.get();
    }

    /**
     * Adds a page to a user's list of pages. This method retrieves the user by their unique identifier, checks if the page is already associated with the user, and if not, adds the page ID to the user's list of pages and saves the updated user back to the database.
     * @param userId The unique identifier of the user to whom the page will be added
     * @param pageId The unique identifier of the page to be added to the user's list of pages
     */
    public void addPageToUser(ObjectId userId, ObjectId pageId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!user.getPages().contains(pageId)) {
            user.getPages().add(pageId);
            userRepository.save(user);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page already associated with user");
        }
    }

    /**
     * Removes a page from a user's list of pages. This method retrieves the user by their unique identifier, checks if the page is currently associated with the user, and if so, removes the page ID from the user's list of pages and saves the updated user back to the database.
     * @param userId The unique identifier of the user from whom the page will be removed
     * @param pageId The unique identifier of the page to be removed from the user's list of pages
     */
    public void removePageFromUser(ObjectId userId, ObjectId pageId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getPages().contains(pageId)) {
            user.getPages().remove(pageId);
            userRepository.save(user);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page not associated with user");
        }
    }

    /**
     * Adds a group to a user's list of groups. This method retrieves the user by their unique identifier, checks if the group is already associated with the user, and if not, adds the group ID to the user's list of groups and saves the updated user back to the database.
     * @param userId The unique identifier of the user to whom the group will be added
     * @param groupId The unique identifier of the group to be added to the user's list of groups
     */
    public void addGroupToUser(ObjectId userId, ObjectId groupId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!user.getGroups().contains(groupId)) {
            user.getGroups().add(groupId);
            userRepository.save(user);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "group already associated with user");
        }
    }

    /**
     * Removes a group from a user's list of groups. This method retrieves the user by their unique identifier, checks if the group is currently associated with the user, and if so, removes the page ID from the user's list of groups and saves the updated user back to the database.
     * @param userId The unique identifier of the user from whom the group will be removed
     * @param groupId The unique identifier of the group to be removed from the user's list of groups
     */
    public void removeGroupFromUser(ObjectId userId, ObjectId groupId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getGroups().contains(groupId)) {
            user.getGroups().remove(groupId);
            userRepository.save(user);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group not associated with user");
        }
    }

    /**
     * Add a friend to a user's list of friends. This method retrieves the user by their unique identifier, checks if the friend is already associated with the user, and if not, adds the friend's ID to the user's list of friends and saves the updated user back to the database.
     * @param userId The unique identifier of the user to whom the friend will be added
     * @param friendId The unique identifier of the friend to be added
     */
    public void addFriend(ObjectId userId, ObjectId friendId) {
        if (friendId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Friend ID is required");
        }
        if (userId.equals(friendId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot add self as friend");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getFriends().contains(friendId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Friend already associated with user");
        }

        if (user.getUsersBanned().contains(friendId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot add banned user as friend");
        }

        if (isUserBannedBy(friendId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot add user: you are banned by this user");
        }

        user.getFriends().add(friendId);
        userRepository.save(user);
    }

    /**
     * Remove a friend from a user's list of friends. This method retrieves the user by their unique identifier, checks if the friend is currently associated with the user, and if so, removes the friend's ID from the user's list of friends and saves the updated user back to the database.
     * @param userId The unique identifier of the user from whom the friend will be removed
     * @param friendId The unique identifier of the friend to be removed
     */
    public void removeFriend(ObjectId userId, ObjectId friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getFriends().contains(friendId)) {
            user.getFriends().remove(friendId);
            userRepository.save(user);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Friend not associated with user");
        }
    }

    /**
     * Check whether a given target user id is present in another user's banned list.
     * @param userId the user whose `usersBanned` list will be checked (e.g. the potential friend)
     * @param targetId the id to look for in that list (e.g. the current authenticated user)
     * @return true if targetId is in user.usersBanned, false otherwise
     */
    public boolean isUserBannedBy(ObjectId userId, ObjectId targetId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return user.getUsersBanned() != null && user.getUsersBanned().contains(targetId);
    }

}
