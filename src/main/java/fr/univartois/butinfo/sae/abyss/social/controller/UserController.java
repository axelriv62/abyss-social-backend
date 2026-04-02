package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.*;
import fr.univartois.butinfo.sae.abyss.social.mapper.UserMapper;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     *
     * @param userService The UserService instance to be used by this controller
     */
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    /**
     * Endpoint for creating a new user.
     * This method accepts a UserDTO in the request body, validates it, converts it to a User entity using the UserMapper, saves it using the UserService, and returns the saved User object in the response.
     *
     * @param userDTO The UserDTO object containing the data for the new user, which is validated using the @Valid annotation
     * @return A ResponseEntity containing the saved User object, with an HTTP status of 201 if the user is successfully created, or 400 if the input data is invalid
     */
    @PostMapping
    @Operation(summary = "Create a new user", description = "Create a new user with the provided data")
    @ApiResponse(responseCode = "200", description = "User successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid data")
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        User savedUser = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponseDTO(savedUser));
    }

    /**
     * Endpoint for deleting a user by their unique identifier.
     * This method retrieves the currently authenticated user from the security context, checks if the user is authenticated, and if so, deletes the user using the UserService.
     * It returns a 204 No Content response if the deletion is successful, or a 401 Unauthorized response if the user is not authenticated.
     */
    @DeleteMapping
    @Operation(summary = "Delete user account", description = "Delete user account")
    @ApiResponse(responseCode = "204", description = "Profile successfully deleted")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Void> deleteById(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        userService.deleteById(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint for updating the current user's profile information (username and profile picture).
     * Only these non-sensitive fields can be updated through this endpoint.
     *
     * @param currentUser The currently authenticated user
     * @param updateDTO The DTO containing the new username and profile picture
     * @return ResponseEntity with the updated user information
     */
    @PatchMapping
    @Operation(summary = "Update current user profile", description = "Update username and profile picture of the authenticated user")
    @ApiResponse(responseCode = "204", description = "Profile successfully updated")
    @ApiResponse(responseCode = "400", description = "Username already in use or invalid data")
    @ApiResponse(responseCode = "401", description = "User not authenticated")
    public ResponseEntity<MessageResponseDTO> updateProfile(@AuthenticationPrincipal User currentUser, @Valid @RequestBody UserUpdateRequestDTO updateDTO) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        userService.updateProfile(currentUser.getId(), updateDTO.username(), updateDTO.profilePicture()
        );
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint for searching users by username.
     *
     * @param username The username fragment to search for. This string is trimmed and validated to ensure it is not blank before performing the search.
     * @return A ResponseEntity containing a list of UserDTO objects whose usernames contain the specified fragment, ignoring case. If the input string is blank after trimming, a ResponseEntity with a 400 Bad Request status is returned. If the user is not authenticated, a ResponseEntity with a 401 Unauthorized status is returned.
     */
    @Operation(summary = "Search posts by username", description = "Lists posts created on the provided username.")
    @ApiResponse(responseCode = "200", description = "Search completed")
    @ApiResponse(responseCode = "400", description = "Username fragment invalid")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDTO>> searchUserByUsername(@RequestParam("username") String username, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<User> matches = userService.searchByUsername(username);
        return ResponseEntity.ok(userMapper.toResponseDTOs(matches));
    }

}
