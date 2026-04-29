package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.*;
import fr.univartois.butinfo.sae.abyss.social.mapper.UserMapper;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling authentication-related operations, such as user registration and login.
 * This controller provides endpoints for registering new users and authenticating existing users, returning JWT tokens upon successful authentication.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * AuthService instance for handling business logic related to authentication operations. This service is injected via the constructor.
     */
    private final AuthService authService;

    /**
     * UserMapper instance for mapping User entities to UserResponseDTOs. This mapper is injected via the constructor and is used to convert User objects to their corresponding DTO representations when returning user information in responses.
     */
    private final UserMapper userMapper;

    /**
     * Constructor for AuthController, injecting the AuthService dependency.
     * @param authService The AuthService instance to be used by this controller.
     * @param userMapper The UserMapper instance to be used for mapping User entities to UserResponseDTOs.
     */
    public AuthController(AuthService authService, UserMapper userMapper) {
        this.authService = authService;
        this.userMapper = userMapper;
    }

    /**
     * Endpoint for registering a new user.
     * This method accepts an AuthRegisterRequestDTO in the request body, validates it, and uses the AuthService to register the user.
     * Upon successful registration, it returns an AuthResponseDTO containing the registered user's information and a JWT token.
     *
     * @param registerDTO The AuthRegisterRequestDTO object containing the data for the new user, which is validated using the @Valid annotation.
     * @return ResponseEntity containing the AuthResponseDTO with the registered user's information and JWT token, with an HTTP status of 201 if the user is successfully registered, or 400 if the input data is invalid.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "201", description = "User has been successfully authenticated and a JWT token is returned")
    @ApiResponse(responseCode = "400", description = "Input data is invalid, that could mean that that some data are already used (for email and username) or that don't meet the requirements")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody AuthRegisterRequestDTO registerDTO) {
        AuthResponseDTO response = authService.register(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint for authenticating an existing user.
     * This method accepts an AuthLoginRequestDTO in the request body, validates it, and uses the AuthService to authenticate the user.
     * Upon successful authentication, it returns an AuthResponseDTO containing the authenticated user's information and a JWT token.
     *
     * @param loginDTO The AuthLoginRequestDTO object containing the email and password for authentication, which is validated using the @Valid annotation.
     * @return ResponseEntity containing the AuthResponseDTO with the authenticated user's information and JWT token, with an HTTP status of 200 if authentication is successful, or 401 if authentication fails.
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return JWT token")
    @ApiResponse(responseCode = "200", description = "User has been successfully authenticated and a JWT token is returned")
    @ApiResponse(responseCode = "401", description = "Authentication failed, that could mean that the email or the password is incorrect")
    public ResponseEntity<AuthResponseDTO> authenticate(@Valid @RequestBody AuthLoginRequestDTO loginDTO) {
        if (authService.isBanned(loginDTO)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new AuthResponseDTO(null, null, null, "Access denied: your account has been banned."));
        }
        AuthResponseDTO response = authService.authenticate(loginDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint for retrieving the profile information of the currently authenticated user.
     * This method uses the @AuthenticationPrincipal annotation to inject the currently authenticated User object, and returns a UserResponseDTO containing the user's profile information.
     *
     * @param currentUser The currently authenticated user, injected by Spring Security using the @AuthenticationPrincipal annotation. This user object is used to retrieve the profile information of the authenticated user and return it in the response.
     * @return ResponseEntity containing the UserResponseDTO with the profile information of the authenticated user, with an HTTP status of 200 if the user is authenticated, or 401 if the user is not authenticated or if the authentication token is missing or invalid.
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Get the profile information of the authenticated user")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully, the profile information of the authenticated user is returned in the response")
    @ApiResponse(responseCode = "401", description = "User not authenticated, that could mean that the user is not authenticated or that the authentication token is missing or invalid")
    public ResponseEntity<UserDTO> getMe(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(userMapper.toDTO(currentUser));
    }

    /**
     * Endpoint for changing the password of the currently authenticated user.
     * Authentication is verified via JWT token, so only the new password is required.
     *
     * @param currentUser The currently authenticated user, injected by Spring Security using the @AuthenticationPrincipal annotation.
     * @param changePasswordDTO The ChangePasswordRequestDTO object containing the new password, which is validated using the @Valid annotation.
     * @return ResponseEntity containing a MessageResponseDTO with a success or error message, with an HTTP status of 200 if the password is successfully changed, or 401 if the user is not authenticated.
     */
    @PatchMapping("/change-password")
    @Operation(summary = "Change the password of the authenticated user")
    @ApiResponse(responseCode = "200", description = "Password has been successfully changed")
    @ApiResponse(responseCode = "401", description = "User not authenticated, JWT token is missing or invalid")
    public ResponseEntity<MessageResponseDTO> changePassword(@AuthenticationPrincipal User currentUser, @Valid @RequestBody ChangePasswordRequestDTO changePasswordDTO) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDTO("User not authenticated"));
        }
        authService.changePassword(currentUser, changePasswordDTO);
        return ResponseEntity.ok(new MessageResponseDTO("Password has been successfully changed"));
    }
}
