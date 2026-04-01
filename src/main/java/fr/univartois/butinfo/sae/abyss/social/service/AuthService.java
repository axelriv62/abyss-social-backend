package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.dto.*;
import fr.univartois.butinfo.sae.abyss.social.mapper.UserMapper;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for handling authentication-related operations such as user registration and login.
 * This class interacts with the UserRepository to manage user data, uses BCryptPasswordEncoder for password hashing, and utilizes JwtService to generate JWT tokens for authenticated users.
 */
@Service
public class AuthService {

    /**
     * UserRepository instance for performing CRUD operations on User entities.
     * This repository is injected via the constructor and is used to save new users and retrieve existing users during authentication.
     */
    private final UserRepository userRepository;

    /**
    * UserMapper instance for converting between User entities and UserResponseDTOs.
    * This mapper is injected via the constructor and is used to convert User objects to UserResponseDTOs when returning authentication responses.
    */
    private final UserMapper userMapper;

    /**
     * BCryptPasswordEncoder instance for hashing user passwords.
     * This encoder is injected via the constructor and is used to securely hash passwords during user registration and to verify passwords during authentication.
     */
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * AuthenticationManager instance for handling authentication operations.
     * This manager is injected via the constructor and is used to authenticate user credentials during the login process.
     */
    private final AuthenticationManager authenticationManager;

    /**
    * JwtService instance for generating JWT tokens.
    * This service is injected via the constructor and is used to generate JWT tokens for authenticated users, which are included in the authentication response.
    */
    private final JwtService jwtService;

    /**
     * Constructor for AuthService, injecting the necessary dependencies for authentication operations.
     *
     * @param userRepository The UserRepository instance for managing user data in the database.
     * @param userMapper The UserMapper instance for converting between User entities and UserResponseDTOs.
     * @param passwordEncoder The BCryptPasswordEncoder instance for hashing user passwords.
     * @param authenticationManager The AuthenticationManager instance for handling authentication operations.
     * @param jwtService The JwtService instance for generating JWT tokens for authenticated users.
     */
    public AuthService(UserRepository userRepository, UserMapper userMapper, BCryptPasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new user based on the provided registration data.
     *
     * This method creates a new User entity from the AuthRegisterRequestDTO, hashes the password using BCryptPasswordEncoder,
     *  saves the user to the database using UserRepository, generates a JWT token for the newly registered user using JwtService,
     *  and returns an AuthResponseDTO containing the user's information and the generated token.
     *
     * @param registerDTO The AuthRegisterRequestDTO object containing the registration data for the new user, including username, email, password, and profile picture.
     * @return An AuthResponseDTO object containing the registered user's information (excluding the password) and a JWT token for authentication, which can be used for subsequent authenticated requests to the application.
     */
    public AuthResponseDTO register(AuthRegisterRequestDTO registerDTO) {
        if (userRepository.findByEmail(registerDTO.email()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.findByUsername(registerDTO.username()).isPresent()) {
            throw new IllegalArgumentException("Username already in use");
        }

        User user = new User(
                registerDTO.username(),
                registerDTO.email(),
                passwordEncoder.encode(registerDTO.password()),
                registerDTO.profilePicture()
        );

        User result = userRepository.save(user);
        String token = jwtService.generateToken(result);
        jwtService.saveToken(token, result.getId());

        UserResponseDTO userResponseDTO = userMapper.toResponseDTO(result);
        return new AuthResponseDTO(userResponseDTO, token, "Bearer");
    }

    /**
     * Authenticates a user based on the provided login credentials.
     * This method uses the AuthenticationManager to authenticate the user's email and password.
     *
     * If authentication is successful, it retrieves the User entity from the database using UserRepository, generates a JWT token for the authenticated user using JwtService,
     *  and returns an AuthResponseDTO containing the user's information and the generated token.
     *
     * @param loginDTO The AuthLoginRequestDTO object containing the login credentials for authentication, including the user's email and password.
     * @return An AuthResponseDTO object containing the authenticated user's information (excluding the password) and a JWT token for authentication, which can be used for subsequent authenticated requests to the application.
     *  If authentication fails, an exception will be thrown indicating that the user was not found or that the credentials are invalid.
     */
    public AuthResponseDTO authenticate(AuthLoginRequestDTO loginDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.email(), loginDTO.password())
        );

        User user = userRepository.findByEmail(loginDTO.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email"));

        String token = jwtService.generateToken(user);
        jwtService.saveToken(token, user.getId());
        UserResponseDTO userResponseDTO = this.userMapper.toResponseDTO(user);
        return new AuthResponseDTO(userResponseDTO, token, "Bearer");
    }
}
