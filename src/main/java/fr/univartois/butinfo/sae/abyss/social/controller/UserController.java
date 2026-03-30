package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.UserDTO;
import fr.univartois.butinfo.sae.abyss.social.mapper.UserMapper;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing User entities, providing endpoints for user-related operations.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    /**
     * UserService instance for handling business logic related to User entities. This service is injected via the constructor.
     */
    private final UserService userService;

    /**
     * UserMapper instance for converting between User entities and UserDTOs. This mapper is injected via the constructor.
     */
    private final UserMapper userMapper;

    /**
     * Constructor for UserController, injecting the UserService dependency.
     * @param userService The UserService instance to be used by this controller
     */
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    /**
     * Endpoint for creating a new user.
     * This method accepts a UserDTO in the request body, validates it, converts it to a User entity using the UserMapper, saves it using the UserService, and returns the saved User object in the response.
     * @param userDTO The UserDTO object containing the data for the new user, which is validated using the @Valid annotation
     * @return A ResponseEntity containing the saved User object, with an HTTP status of 200 OK
     */
    @PostMapping
    public ResponseEntity<User> save(@Valid @RequestBody UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        User savedUser = userService.save(user);
        return ResponseEntity.ok(savedUser);
    }
}
