package fr.univartois.butinfo.sae.abyss.social.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import fr.univartois.butinfo.sae.abyss.social.model.Token;

import java.util.Optional;

/**
 * Repository interface for managing Token entities in the MongoDB database.
 * This interface extends MongoRepository, providing CRUD operations and custom query methods for Token entities.
 */
public interface TokenRepository extends MongoRepository<Token, ObjectId> {

    /**
     * Find a token by its string representation.
     * @param token The string representation of the token to find.
     * @return An Optional containing the found Token, or empty if no token matches the provided string.
     */
    Optional<Token> findByToken(String token);

}
