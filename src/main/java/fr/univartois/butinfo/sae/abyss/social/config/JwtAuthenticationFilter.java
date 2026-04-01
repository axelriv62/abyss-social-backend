package fr.univartois.butinfo.sae.abyss.social.config;

import fr.univartois.butinfo.sae.abyss.social.service.JwtService;
import fr.univartois.butinfo.sae.abyss.social.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter for JWT authentication, intercepting incoming HTTP requests to validate JWT tokens and set the security context accordingly.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Service for handling JWT operations, such as token generation and validation.
     * This service is injected via the constructor and is used to extract the username from the token and validate the
     */
    private final JwtService jwtService;

    /**
     * Service for managing user-related operations, such as loading user details by username.
     * This service is injected via the constructor and is used to retrieve user details for authentication purposes.
     */
    private final UserService userService;

    /**
     * Constructor for JwtAuthenticationFilter, injecting the necessary dependencies for JWT authentication.
     * @param jwtService The JwtService instance for handling JWT operations, such as token validation and username extraction.
     * @param userService The UserService instance for managing user-related operations, such as loading user details by username for authentication purposes.
     */
    public JwtAuthenticationFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    /**
     * Intercepts incoming HTTP requests to validate JWT tokens and set the security context accordingly.
     * This method checks for the presence of an Authorization header, extracts the JWT token, validates it, and if valid, sets the authentication in the security context for the current request.
     * @param request The HttpServletRequest object representing the incoming HTTP request, which may contain an Authorization header with a JWT token for authentication.
     * @param response The HttpServletResponse object representing the HTTP response that will be sent back to the client after processing the request, which may be modified based on the authentication outcome.
     * @param filterChain The FilterChain object representing the chain of filters that the request will pass through, allowing the filter to delegate to the next filter in the chain after processing the JWT authentication.
     * @throws ServletException if an error occurs during the processing of the request, such as issues with the filter chain or authentication process.
     * @throws IOException if an I/O error occurs during the processing of the request, such as issues with reading the request or writing the response.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = userService.loadUserByUsername(username);

            if (jwtService.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

}
