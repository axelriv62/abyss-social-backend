package fr.univartois.butinfo.sae.abyss.social.dto;

import fr.univartois.butinfo.sae.abyss.social.model.ROLES;

/**
 * Data Transfer Object for User entity, used for transferring data between layers (e.g. controller and service).
 * Contains the user's information that will be sent in responses, such as user ID, username, email, profile picture, and role.
 * @param id The unique identifier of the user, represented as an ObjectId from MongoDB.
 * @param username The username of the user, which is a string that can be used to identify the user in the application.
 * @param email The email address of the user, which is a string that can be used for communication and authentication purposes.
 * @param profilePicture The profile picture of the user, encoded as a data URL ready to be rendered by a client.
 * @param role The role of the user, represented as an enum (ROLES), which can be used to determine the user's permissions and access levels within the application.
 */
public record UserResponseDTO(
        String id,
        String username,
        String email,
        String profilePicture,
        ROLES role
) {}
