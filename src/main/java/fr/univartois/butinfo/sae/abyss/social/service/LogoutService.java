package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import fr.univartois.butinfo.sae.abyss.social.model.Token;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class for handling user logout operations. This class implements the LogoutHandler interface from Spring Security, allowing it to be used as a component in the logout process.
 * The primary responsibility of this service is to invalidate the user's authentication token upon logout by removing it from the TokenRepository.
 */
@Service
public class LogoutService implements LogoutHandler {

    /**
     * TokenRepository instance for managing authentication tokens.
     * This repository is injected via the constructor and is used to find and delete tokens during the logout process.
     */
    private final TokenRepository tokenRepository;

    /**
     * Constructor for LogoutService, injecting the TokenRepository dependency.
     * @param tokenRepository The TokenRepository instance to be used for managing authentication tokens during the logout process, allowing the service to find and delete tokens as needed when a user logs out.
     */
    public LogoutService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Handles the logout process by invalidating the user's authentication token.
     * This method is called when a user initiates a logout request, and it checks for the presence of an Authorization header containing a Bearer token.
     * If such a token is found, it extracts the token string, looks it up in the TokenRepository, and if a matching token is found, it deletes it from the repository to effectively invalidate the user's session.
     * @param request The HttpServletRequest object representing the incoming logout request, which may contain an Authorization header with a Bearer token that needs to be invalidated during the logout process.
     * @param response The HttpServletResponse object representing the HTTP response that will be sent back to the client after processing the logout request, which may be modified based on the outcome of the logout operation.
     * @param authentication The Authentication object representing the current authentication state of the user, which may be used to perform additional checks or operations during the logout process if needed.
     */
    @Override
    public void logout(HttpServletRequest request, @NonNull HttpServletResponse response, Authentication authentication) {
        String authHeader = request.getHeader("Authorization");
        if ((authHeader == null) || (!authHeader.startsWith("Bearer "))) {
            return;
        }
        String jwt = authHeader.substring(7);
        Optional<Token> token = tokenRepository.findByUserToken(jwt);
        token.ifPresent(value -> {
            value.setRevoked(true);
            tokenRepository.save(value);
        });
    }
}
