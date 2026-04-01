package fr.univartois.butinfo.sae.abyss.social.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application, providing centralized handling of exceptions thrown by controllers.
 * This class uses @RestControllerAdvice to intercept exceptions and return appropriate HTTP responses with error messages.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles BadCredentialsException, which is thrown when authentication fails due to invalid credentials.
     * This method returns a 401 Unauthorized response with a JSON body containing an error message.
     * @return ResponseEntity containing a JSON object with an "error" key and a message indicating invalid email or password, with an HTTP status of 401 Unauthorized.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials() {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handles MethodArgumentNotValidException, which is thrown when validation of request body fails due to invalid input data.
     * This method extracts validation error messages and returns a 400 Bad Request response with a JSON body containing the error messages.
     * @param exception The MethodArgumentNotValidException instance that was thrown during validation failure of request body parameters.
     * @return ResponseEntity containing a JSON object with an "error" key and the validation error message, with an HTTP status of 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
