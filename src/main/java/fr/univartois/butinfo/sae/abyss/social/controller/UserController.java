package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.UserDTO;
import fr.univartois.butinfo.sae.abyss.social.mapper.UserMapper;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * @return A ResponseEntity containing the saved User object, with an HTTP status of 201 if the user is successfully created, or 400 if the input data is invalid
     */
    @PostMapping
    @Operation(summary = "Create a new user", description = "Create a new user with the provided data")
    @ApiResponse(responseCode = "200", description = "User successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid data")
    public ResponseEntity<UserDTO> save(@Valid @RequestBody UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        User savedUser = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDTO(savedUser));
    }

    /**
     * Endpoint for deleting a user by their unique identifier. This method accepts a user ID as a path variable, deletes the corresponding user using the UserService, and returns an appropriate HTTP status code in the response.
     * @param id The unique identifier of the user to be deleted, provided as a path variable
     * @return A ResponseEntity with an HTTP status of 204 if the user is successfully deleted, or 404 if the user with the specified ID is not found
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user by ID", description = "Delete a user with the specified ID")
    @ApiResponse(responseCode = "204", description = "User successfully deleted")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Void> deleteById(@PathVariable ObjectId id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
