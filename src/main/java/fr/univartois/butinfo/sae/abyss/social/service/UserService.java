package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Service class for managing User entities, providing business logic for user-related operations.
 */
@Service
public class UserService {

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
}
