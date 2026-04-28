package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.*;
import fr.univartois.butinfo.sae.abyss.social.mapper.UserMapper;
import fr.univartois.butinfo.sae.abyss.social.model.ROLES;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private static final String ADMIN_ACCESS_DENIED = "Access denied: You must be logged in as an admin to perform this action.";

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
    @ApiResponse(responseCode = "200", description = "User successfully created and returned in the response")
    @ApiResponse(responseCode = "400", description = "Invalid data, that could mean that some data are already used (for email and username) or that don't meet the requirements")
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        User savedUser = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponseDTO(savedUser));
    }

    /**
     * Endpoint for deleting the current user.
     * This method retrieves the currently authenticated user from the security context, checks if the user is authenticated, and if so, deletes the user using the UserService.
     * It returns a 204 No Content response if the deletion is successful, or a 401 Unauthorized response if the user is not authenticated.
     */
    @DeleteMapping
    @Operation(summary = "Delete user account", description = "Delete user account of the authenticated user")
    @ApiResponse(responseCode = "204", description = "Profile successfully deleted, the account has been successfully deleted from the database and that the user will no longer be able to access their account or any associated data")
    @ApiResponse(responseCode = "404", description = "User not found, that could mean that the user does not exist in the database or that the user is already deleted")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User currentUser) {
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
     * @return ResponseEntity with the updated user information
     */
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update current user profile", description = "Update username and/or email and/or profile picture of the authenticated user. Only provided fields will be updated")
    @ApiResponse(responseCode = "204", description = "Profile successfully updated")
    @ApiResponse(responseCode = "400", description = "Username or email already in use or invalid data")
    @ApiResponse(responseCode = "401", description = "User not authenticated")
    public ResponseEntity<MessageResponseDTO> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) MultipartFile profilePicture) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Binary profilePictureBinary = null;
        String profilePictureContentType = null;

        if (profilePicture != null && !profilePicture.isEmpty()) {
            try {
                profilePictureBinary = new Binary(profilePicture.getBytes());
                profilePictureContentType = profilePicture.getContentType();
            } catch (IOException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        userService.updateProfile(currentUser.getId(), username, email, profilePictureBinary, profilePictureContentType);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint for searching users by username.
     *
     * @param username The username fragment to search for. This string is trimmed and validated to ensure it is not blank before performing the search.
     * @return A ResponseEntity containing a list of UserDTO objects whose usernames contain the specified fragment, ignoring case. If the input string is blank after trimming, a ResponseEntity with a 400 Bad Request status is returned. If the user is not authenticated, a ResponseEntity with a 401 Unauthorized status is returned.
     */
    @Operation(summary = "Search posts by username", description = "Lists posts created on the provided username fragment, that could be a full username or just a part of it. The search is case-insensitive and will return all users whose usernames contain the provided fragment")
    @ApiResponse(responseCode = "200", description = "Search completed successfully, a list of users whose usernames contain the provided fragment is returned in the response")
    @ApiResponse(responseCode = "400", description = "Username fragment invalid, that could mean that the provided username fragment is blank or does not meet the validation requirements")
    @ApiResponse(responseCode = "401", description = "Unauthorized, that could mean that the user is not authenticated or that the authentication token is missing or invalid")
    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDTO>> searchUserByUsername(@RequestParam("username") String username, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<User> matches = userService.searchByUsername(username);
        return ResponseEntity.ok(userMapper.toResponseDTOs(matches));
    }

    /**
     * Endpoint for adding a friend to the current user's friend list.
     * This method retrieves the currently authenticated user, checks if the user is authenticated, and if so, adds the specified friend ID to the user's list of friends using the UserService.
     *
     */
    @PatchMapping("/friends/add")
    @Operation(summary = "Add friend to friend list", description = "Add a friend to the authenticated user's friend list")
    @ApiResponse(responseCode = "200", description = "Friend successfully added to friend list")
    @ApiResponse(responseCode = "400", description = "Friend ID is required or friend already in friend list, that could mean that the friend ID is missing from the request or that the specified friend is already in the authenticated user's friend list")
    @ApiResponse(responseCode = "401", description = "User not authenticated, that could mean that the user is not authenticated or that the authentication token is missing or invalid")

    public ResponseEntity<MessageResponseDTO> addFriend(@AuthenticationPrincipal User currentUser, @RequestParam("friendId") ObjectId friendId) {
        userService.addFriend(currentUser.getId(), friendId);
        return ResponseEntity.ok(new MessageResponseDTO("Friend successfully added to friend list"));
    }

    /**
     * Endpoint for removing a friend to the current user's friend list.
     * This method retrieves the currently authenticated user, checks if the user is authenticated, and if so, removes the specified friend ID to the user's list of friends using the UserService.
     */
    @PatchMapping("/friends/remove")
    @Operation(summary = "Remove friend from friend list", description = "Remove a friend from the authenticated user's friend list")
    @ApiResponse(responseCode = "200", description = "Friend successfully removed from friend list")
    @ApiResponse(responseCode = "400", description = "Friend ID is required or friend not in friend list, that could mean that the friend ID is missing from the request or that the specified friend is not in the authenticated user's friend list")
    @ApiResponse(responseCode = "403", description = "User not authenticated, that could mean that the user is not authenticated or that the authentication token is missing or invalid")
    public ResponseEntity<MessageResponseDTO> removeFriend(@AuthenticationPrincipal User currentUser, @RequestParam("friendId") ObjectId friendId) {
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO("You're not authenticated"));
        }
        if (friendId == null) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Friend ID is required"));
        }
        if (!currentUser.getFriends().contains(friendId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponseDTO("Friend not in friend list"));
        }
        userService.removeFriend(currentUser.getId(), friendId);
        return ResponseEntity.ok(new MessageResponseDTO("Friend successfully removed from friend list"));
    }
    /**
     * Endpoint for banning a user by their ID. This operation is restricted to admin users only.
     * @param userId The ID of the user to be banned
     * @param currentUser The currently authenticated user, whose role will be checked to ensure they have admin privileges before allowing the ban operation to proceed
     * @return ResponseEntity with a message indicating the result of the ban operation, with a 200 OK status if the user is successfully banned, or a 403 Forbidden status if the current user does not have admin privileges
     */
    @PatchMapping("/{userId}/ban")
    @Operation(summary = "Ban a user", description = "Ban a user by their ID (admin only)")
    @ApiResponse(responseCode = "200", description = "User successfully banned")
    @ApiResponse(responseCode = "403", description = "Forbidden: Only admins can ban users, that could mean that the user is not authenticated or that the user does not have admin privileges")
    public ResponseEntity<MessageResponseDTO> banUser(@PathVariable ObjectId userId, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null || currentUser.getRole() != ROLES.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponseDTO(ADMIN_ACCESS_DENIED));
        }

        userService.banUser(userId);
        return ResponseEntity.ok(new MessageResponseDTO("User successfully banned"));
    }

    /**
     * Endpoint for unbanning a user by their ID. This operation is restricted to admin users only.
     * @param userId The ID of the user to be unbanned
     * @param currentUser The currently authenticated user, whose role will be checked to ensure they have admin privileges before allowing the unban operation to proceed
     * @return ResponseEntity with a message indicating the result of the unban operation, with a 200 OK status if the user is successfully unbanned, or a 403 Forbidden status if the current user does not have admin privileges
     */
    @PatchMapping("/{userId}/unban")
    @Operation(summary = "Unban a user", description = "Unban a user by their ID (admin only)")
    @ApiResponse(responseCode = "200", description = "User successfully unbanned")
    @ApiResponse(responseCode = "403", description = "Forbidden: Only admins can unban users, that could mean that the user is not authenticated or that the user does not have admin privileges")
    public ResponseEntity<MessageResponseDTO> unbanUser(@PathVariable ObjectId userId, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null || currentUser.getRole() != ROLES.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponseDTO(ADMIN_ACCESS_DENIED));
        }

        userService.unbanUser(userId);
        return ResponseEntity.ok(new MessageResponseDTO("User successfully unbanned"));
    }

    /**
     * Endpoint for changing a user's role by their ID. This operation is restricted to admin users only.
     * @param userId The ID of the user whose role is to be changed
     * @param newRole The new role to be assigned to the user, passed as a request parameter
     * @param currentUser The currently authenticated user, whose role will be checked to ensure they have admin privileges before allowing the role change operation to proceed
     * @return ResponseEntity with a message indicating the result of the role change operation, with a 200 OK status if the user's role is successfully changed, or a 403 Forbidden status if the current user does not have admin privileges
     */
    @PatchMapping("/{userId}/role")
    @Operation(summary = "Change user role", description = "Change the role of a user by their ID (admin only)")
    @ApiResponse(responseCode = "200", description = "User role successfully changed")
    @ApiResponse(responseCode = "403", description = "Forbidden: Only admins can change user roles, that could mean that the user is not authenticated or that the user does not have admin privileges")
    public ResponseEntity<MessageResponseDTO> changeUserRole(@PathVariable ObjectId userId, @RequestParam ROLES newRole, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null || currentUser.getRole() != ROLES.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponseDTO(ADMIN_ACCESS_DENIED));
        }

        userService.changeUserRole(userId, newRole);
        return ResponseEntity.ok(new MessageResponseDTO("User role successfully changed to " + newRole));
    }

    @PatchMapping("/{userId}/block")
    @Operation(summary = "Block a user", description = "Add the specified user to the authenticated user's banned list")
    @ApiResponse(responseCode = "200", description = "User successfully blocked")
    @ApiResponse(responseCode = "400", description = "Invalid request, that could mean that the user to block ID is missing from the request or that the user is trying to block themselves, an admin, or a user who is already blocked")
    @ApiResponse(responseCode = "401", description = "User not authenticated, that could mean that the user is not authenticated or that the authentication token is missing or invalid")
    public ResponseEntity<MessageResponseDTO> blockUser(@PathVariable("userId") ObjectId userToBlockId, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null || userToBlockId == null) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO("current user or user to block is null"));
        }
        if (currentUser.getId().equals(userToBlockId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponseDTO("Cannot block yourself"));
        }
        if (userService.isAdmin(userToBlockId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponseDTO("Cannot block an admin"));
        }
        if (currentUser.getUsersBanned().contains(userToBlockId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponseDTO("Cannot block a blocked user"));
        }

        userService.blockUser(currentUser.getId(), userToBlockId);
        return ResponseEntity.ok(new MessageResponseDTO("User successfully blocked"));
    }

    @PatchMapping("/{userId}/unblock")
    @Operation(summary = "Unblock a user", description = "Remove the specified user from the authenticated user's banned list")
    @ApiResponse(responseCode = "200", description = "User successfully unblocked")
    @ApiResponse(responseCode = "400", description = "Invalid request, that could mean that the user to unblock ID is missing from the request or that the user is trying to unblock themselves or a user who is not currently blocked")
    @ApiResponse(responseCode = "401", description = "User not authenticated, that could mean that the user is not authenticated or that the authentication token is missing or invalid")
    public ResponseEntity<MessageResponseDTO> unblockUser(@PathVariable("userId") ObjectId userToUnblockId, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null || userToUnblockId == null) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO("current user or user to unblock is null"));
        }
        if (!currentUser.getUsersBanned().contains(userToUnblockId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponseDTO("Cannot unblock a user who is not blocked"));
        }
        userService.unblockUser(currentUser.getId(), userToUnblockId);
        return ResponseEntity.ok(new MessageResponseDTO("User successfully unblocked"));
    }

    @GetMapping("/{userId}/pages")
    @Operation(summary = "Get all pages of a user")
    @ApiResponse(responseCode = "200", description = "Pages retrieved successfully")
    @ApiResponse(responseCode = "401", description = "User not authenticated, that could mean that the user is not authenticated or that the authentication token is missing or invalid")
    @ApiResponse(responseCode = "404", description = "User not found, that could mean that the user with the specified ID does not exist in the database")
    public List<String> getPages(@PathVariable ObjectId userId, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return List.of();
        }
        return userService.getUserPages(userId).stream()
                .map(ObjectId::toHexString)
                .toList();
    }

    @GetMapping("/{userId}/groups")
    @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    @ApiResponse(responseCode = "401", description = "User not authenticated, that could mean that the user is not authenticated or that the authentication token is missing or invalid")
    @ApiResponse(responseCode = "404", description = "User not found, that could mean that the user with the specified ID does not exist in the database")
    public List<String> getGroups(@PathVariable ObjectId userId, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return List.of();
        }
        return userService.getUserGroups(userId).stream()
                .map(ObjectId::toHexString)
                .toList();
    }

    @GetMapping("/{userId}/posts")
    @Operation(summary = "Get all posts of a user, excluding posts from groups or pages the current user is not a member of")
    @ApiResponse(responseCode = "200", description = "Posts retrieved successfully, a list of posts created by the specified user is returned in the response, excluding any posts that belong to groups or pages that the current user is not a member of")
    @ApiResponse(responseCode = "401", description = "User not authenticated, that could mean that the user is not authenticated or that the authentication token is missing or invalid")
    @ApiResponse(responseCode = "404", description = "User not found, that could mean that the user with the specified ID does not exist in the database")
    public List<PostDTO> getPosts(@PathVariable ObjectId userId, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return List.of();
        }
        return userService.getUsersPosts(userId);
    }

    /**
     * Endpoint for retrieving the list of all registered users.
     * This operation is restricted to admin users only.
     *
     * @param currentUser The currently authenticated user, whose role will be checked to ensure they have admin privileges
     * @return ResponseEntity containing a list of UserResponseDTOs for all registered users with an HTTP status of 200 if successful, or 403 if the user doesn't have admin privileges
     */
    @GetMapping("/all")
    @Operation(summary = "Get all registered users", description = "Retrieve a list of all registered users (admin only)")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully, a list of all registered users is returned in the response")
    @ApiResponse(responseCode = "403", description = "Forbidden: Only admins can view all users, that could mean that the user is not authenticated or that the user does not have admin privileges")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null || currentUser.getRole() != ROLES.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(List.of());
        }

        List<User> users = userService.getAllUsers();
        List<UserResponseDTO> userDTOs = userMapper.toResponseDTOs(users);
        return ResponseEntity.ok(userDTOs);
    }

    /**
     * Endpoint for retrieving the list of users blocked by the authenticated user.
     * This method is only accessible to authenticated users.
     *
     * @param currentUser The currently authenticated user
     * @return ResponseEntity containing a list of UserResponseDTOs for all blocked users with an HTTP status of 200 if successful, or 401 if not authenticated
     */
    @GetMapping("/blocked")
    @Operation(summary = "Get blocked users list", description = "Retrieve a list of all users blocked by the authenticated user")
    @ApiResponse(responseCode = "200", description = "Blocked users retrieved successfully, a list of all users blocked by the authenticated user is returned in the response")
    @ApiResponse(responseCode = "401", description = "User not authenticated, that could mean that the user is not authenticated or that the authentication token is missing or invalid")
    public ResponseEntity<List<UserResponseDTO>> getBlockedUsers(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(List.of());
        }

        List<User> blockedUsers = userService.getBlockedUsers(currentUser.getId());
        List<UserResponseDTO> blockedUserDTOs = userMapper.toResponseDTOs(blockedUsers);
        return ResponseEntity.ok(blockedUserDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a user's information by their ID")
    @ApiResponse(responseCode = "200", description = "User retrieved successfully, the user's information is returned in the response")
    @ApiResponse(responseCode = "401", description = "User not authenticated, that could mean that the user is not authenticated or that the authentication token is missing or invalid")
    @ApiResponse(responseCode = "404", description = "User not found, that could mean that the user with the specified ID does not exist in the database")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable ObjectId id, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.getById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(userMapper.toResponseDTO(user));
    }

}
