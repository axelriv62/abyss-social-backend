package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.repository.UserRepository;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

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
     * Loads a user by their username (in this case, email) for authentication purposes. This method is required by the UserDetailsService interface and is used by Spring Security to retrieve user details during the authentication process.
     * @param username the username identifying the user whose data is required (in this implementation, the email is used as the username)
     * @return
     */
    @Override
    public UserDetails loadUserByUsername(@NonNull String username) {
        Optional<User> userOptional = userRepository.findByEmail(username);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return userOptional.get();
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

}
