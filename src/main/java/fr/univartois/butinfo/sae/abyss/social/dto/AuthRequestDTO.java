package fr.univartois.butinfo.sae.abyss.social.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthRequestDTO {

    /**
     * The email for authentication, must be a valid email address and not blank.
     */
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is necessary")
    private String email;

    /**
     * The password for authentication.
     */
    @NotBlank(message = "Password is necessary")
    private String password;

    /**
     * Get the email for authentication.
     * @return The email, must be a valid email address and not blank.
     */
    public String getEmail() { return email; }

    /**
     * Set the email for authentication.
     * @param email The email to be set, must be a valid email address and not blank.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Get the password for authentication.
     * @return The password, must not be blank.
     */
    public String getPassword() { return password; }

    /**
     * Set the password for authentication.
     * @param password The password to be set, must not be blank.
     */
    public void setPassword(String password) { this.password = password; }
}
