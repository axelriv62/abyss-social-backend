package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.AuthRegisterRequestDTO;
import fr.univartois.butinfo.sae.abyss.social.dto.AuthLoginRequestDTO;
import fr.univartois.butinfo.sae.abyss.social.dto.AuthResponseDTO;
import fr.univartois.butinfo.sae.abyss.social.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     * Constructor for AuthController, injecting the AuthService dependency.
     * @param authService The AuthService instance to be used by this controller.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
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
    public ResponseEntity<AuthResponseDTO> authenticate(@Valid @RequestBody AuthLoginRequestDTO loginDTO) {
        AuthResponseDTO response = authService.authenticate(loginDTO);
        return ResponseEntity.ok(response);
    }
}
