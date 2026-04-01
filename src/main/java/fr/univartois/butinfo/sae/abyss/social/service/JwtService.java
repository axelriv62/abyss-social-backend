package fr.univartois.butinfo.sae.abyss.social.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Service class responsible for handling JWT (JSON Web Token) operations such as token generation, validation, and claim extraction.
 * This class uses the JJWT library to create and parse JWT tokens, and it retrieves configuration properties for the secret key and token expiration time from the application properties.
 */
@Service
public class JwtService {

    /**
     * Secret key used for signing JWT tokens. This key is injected from the application properties using the @Value annotation, and it is decoded from Base64 format to create a SecretKey instance for signing and verifying JWT tokens.
     */
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    /**
     * Expiration time for JWT tokens in milliseconds. This value is injected from the application properties using the @Value annotation, and it is used to set the expiration time when generating JWT tokens.
     */
    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    /**
     * Generates a JWT token for the given UserDetails.
     * This method creates a JWT token with the user's username as the subject, sets the issued at and expiration times, and signs the token using the secret key. The generated token is returned as a String.
     * @param userDetails The UserDetails object containing the user's information, which is used to set the subject of the JWT token and to include any additional claims if needed.
     * @return A String representing the generated JWT token, which can be used for authentication and authorization purposes in the application.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(Map.of(), userDetails);
    }

    /**
     * Generates a JWT token for the given UserDetails with additional claims.
     * This method creates a JWT token with the user's username as the subject, includes any additional claims provided in the extraClaims map, sets the issued at and expiration times, and signs the token using the secret key. The generated token is returned as a String.
     * @param extraClaims A Map containing any additional claims to be included in the JWT token, which can be used to store extra information about the user or the authentication context. This map is merged with the standard claims when generating the token.
     * @param userDetails The UserDetails object containing the user's information, which is used to set the subject of the JWT token and to include any additional claims if needed.
     * @return A String representing the generated JWT token, which can be used for authentication and authorization purposes in the application. The token includes the user's username as the subject, any additional claims provided, and is signed with the secret key to ensure its integrity and authenticity.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Builds a JWT token with the given extra claims, user details, and expiration time.
     * This method creates a JWT token with the user's username as the subject, includes any additional claims provided in the extraClaims map, sets the issued at and expiration times based on the current time and the provided expiration duration, and signs the token using the secret key. The generated token is returned as a String.
     * @param extraClaims A Map containing any additional claims to be included in the JWT token, which can be used to store extra information about the user or the authentication context. This map is merged with the standard claims when generating the token.
     * @param userDetails The UserDetails object containing the user's information, which is used to set the subject of the JWT token and to include any additional claims if needed.
     * @param expiration The duration in milliseconds for which the JWT token should be valid. This value is used to calculate the expiration time of the token by adding it to the current time when generating the token.
     * @return A String representing the generated JWT token, which can be used for authentication and authorization purposes in the application. The token includes the user's username as the subject, any additional claims provided, and is signed with the secret key to ensure its integrity and authenticity.
     */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Validates the given JWT token against the provided UserDetails.
     * This method extracts the username from the token and checks if it matches the username in the UserDetails, and also checks if the token has expired. If the token is valid (i.e., the username matches and the token is not expired), it returns true; otherwise, it returns false.
     * @param token The JWT token to be validated, which is typically extracted from the Authorization header of an incoming HTTP request. This token is parsed to extract the username and expiration information for validation purposes.
     * @param userDetails The UserDetails object containing the user's information, which is used to compare the username extracted from the token with the username in the UserDetails, and to ensure that the token is valid for the authenticated user.
     * @return
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return !isTokenExpired(token) && (username.equals(userDetails.getUsername()));
    }

    /**
     * Checks if the given JWT token has expired by comparing the expiration time extracted from the token with the current time.
     * If the token's expiration time is before the current time, it returns true, indicating that the token has expired; otherwise, it returns false.
     * @param token The JWT token to be checked for expiration, which is typically extracted from the Authorization header of an incoming HTTP request. This token is parsed to extract the expiration time for validation purposes.
     * @return A boolean value indicating whether the JWT token has expired (true) or is still valid (false), based on the expiration time extracted from the token compared to the current time.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the username (subject) from the given JWT token by parsing the token and retrieving the subject claim. This method uses the extractClaim method to extract the subject claim from the token, which represents the username of the authenticated user.
     * @param token The JWT token from which to extract the username, which is typically extracted from the Authorization header of an incoming HTTP request. This token is parsed to extract the subject claim, which represents the username of the authenticated user.
     * @return A String representing the username (subject) extracted from the JWT token, which can be used for authentication and authorization purposes in the application. The username is typically used to identify the authenticated user and to retrieve their details from the database or user service for further processing.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration time from the given JWT token by parsing the token and retrieving the expiration claim. This method uses the extractClaim method to extract the expiration claim from the token, which represents the time at which the token will expire and become invalid for authentication purposes.
     * @param token The JWT token from which to extract the expiration time, which is typically extracted from the Authorization header of an incoming HTTP request. This token is parsed to extract the expiration claim, which represents the time at which the token will expire and become invalid for authentication purposes.
     * @return A Date object representing the expiration time extracted from the JWT token, which indicates when the token will expire and become invalid for authentication purposes. This expiration time is used to determine if the token is still valid or if it has expired, based on the current time compared to the extracted expiration time.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the given JWT token using a claims resolver function. This method parses the token to retrieve all claims and then applies the provided claimsResolver function to extract the desired claim from the claims. The extracted claim is returned as a generic type T, allowing for flexibility in retrieving different types of claims from the token.
     * @param token The JWT token from which to extract the claim, which is typically extracted from the Authorization header of an incoming HTTP request. This token is parsed to retrieve all claims, and the provided claimsResolver function is applied to extract the specific claim of interest from the claims.
     * @param claimsResolver A Function that takes a Claims object as input and returns a specific claim of type T. This function is used to extract the desired claim from the claims retrieved from the token, allowing for flexibility in retrieving different types of claims based on the application's needs.
     * @return A generic type T representing the specific claim extracted from the JWT token using the provided claimsResolver function. This allows for flexibility in retrieving different types of claims from the token, such as the subject (username), expiration time, or any custom claims that may be included in the token.
     * @param <T> The generic type representing the specific claim to be extracted from the JWT token, which can be any type depending on the claim being retrieved (e.g., String for subject, Date for expiration, etc.). This allows for flexibility in retrieving different types of claims from the token based on the application's needs.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the given JWT token by parsing the token using the JJWT library. This method verifies the token's signature using the secret key and retrieves the claims contained in the token's payload. The extracted claims are returned as a Claims object, which can be used to access specific claims such as the subject (username), expiration time, and any additional claims included in the token.
     * @param token The JWT token from which to extract all claims, which is typically extracted from the Authorization header of an incoming HTTP request. This token is parsed using the JJWT library to verify its signature and retrieve the claims contained in the token's payload.
     *              The extracted claims are returned as a Claims object, which can be used to access specific claims such as the subject (username), expiration time, and any additional claims included in the token for authentication and authorization purposes in the application.
     * @return A Claims object representing all the claims extracted from the JWT token, which includes standard claims such as the subject (username) and expiration time, as well as any additional claims that may be included in the token's payload. This Claims object can be used to access specific claims for authentication and authorization purposes in the application.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Retrieves the signing key for JWT token generation and validation by decoding the secret key from Base64 format and creating a SecretKey instance using the JJWT library's Keys utility class. This signing key is used to sign JWT tokens when generating them and to verify the signature of incoming JWT tokens during validation.
     * @return A SecretKey instance representing the signing key for JWT token generation and validation, which is created by decoding the secret key from Base64 format and using the JJWT library's Keys utility class to create a SecretKey instance.
     *  This signing key is used to sign JWT tokens when generating them and to verify the signature of incoming JWT tokens during validation, ensuring the integrity and authenticity of the tokens used for authentication and authorization in the application.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}