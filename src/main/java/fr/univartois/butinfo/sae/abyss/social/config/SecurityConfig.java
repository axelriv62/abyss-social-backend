package fr.univartois.butinfo.sae.abyss.social.config;

import fr.univartois.butinfo.sae.abyss.social.service.LogoutService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration class for Spring Security, defining the security filter chain and authentication provider for the application.
 * This class is annotated with @Configuration to indicate that it provides bean definitions, and @EnableWebSecurity to enable Spring Security's web security support.
 * It defines a SecurityFilterChain bean that configures HTTP security settings, including disabling CSRF protection, setting the authentication provider, configuring session management to be stateless, and defining authorization rules for HTTP requests.
 * It also adds a custom JwtAuthenticationFilter to the security filter chain to handle JWT authentication for incoming requests.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * AuthenticationProvider instance for handling authentication logic, such as validating user credentials and generating authentication tokens.
     * This provider is injected via the constructor and is used in the security filter chain to authenticate incoming requests based on the configured authentication logic.
     */
    private final AuthenticationProvider authenticationProvider;

    /**
     * JwtAuthenticationFilter instance for handling JWT authentication, intercepting incoming HTTP requests to validate JWT tokens and set the security context accordingly.
     * This filter is injected via the constructor and is added to the security filter chain to ensure that JWT authentication is applied to incoming requests before the standard UsernamePasswordAuthenticationFilter, allowing for token-based authentication in the application.
     */
    private final JwtAuthenticationFilter jwtFilter;

    /**
     * LogoutService instance for handling user logout operations, such as invalidating authentication tokens upon logout requests.
     * This service is injected via the constructor and is used in the security filter chain to handle logout requests by invalidating the user's authentication token, ensuring that the user's session is effectively terminated when they log out of the application.
     */
    private final LogoutService logoutService;

    /**
     * Constructor for SecurityConfig, injecting the necessary dependencies for configuring Spring Security.
     * @param jwtFilter The JwtAuthenticationFilter instance for handling JWT authentication, which will be added to the security filter chain to validate JWT tokens for incoming requests.
     * @param authenticationProvider The AuthenticationProvider instance for handling authentication logic, which will be used in the security filter chain to authenticate incoming requests based on the configured authentication logic.
     */
    public SecurityConfig(JwtAuthenticationFilter jwtFilter, AuthenticationProvider authenticationProvider, LogoutService logoutService) {
        this.authenticationProvider = authenticationProvider;
        this.jwtFilter = jwtFilter;
        this.logoutService = logoutService;
    }

    /**
     * Defines the security filter chain for the application, configuring HTTP security settings such as disabling CSRF protection, setting the authentication provider, configuring session management to be stateless, and defining authorization rules for HTTP requests.
     * @param http The HttpSecurity object used to configure the security settings for HTTP requests, allowing for customization of various aspects of web security such as authentication, authorization, and session management.
     * @return A SecurityFilterChain object representing the configured security filter chain for the application, which will be used by Spring Security to apply the defined security settings to incoming HTTP requests.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(authenticationProvider)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(r -> r
                        .requestMatchers("/auth/register", "/auth/login").permitAll()
                        .requestMatchers("/auth/logout").authenticated()
                        .anyRequest().authenticated())
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(logoutService)
                        .logoutSuccessHandler((req, resp, auth) -> SecurityContextHolder.clearContext()))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
