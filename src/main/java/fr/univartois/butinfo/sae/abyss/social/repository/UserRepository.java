package fr.univartois.butinfo.sae.abyss.social.repository;

import fr.univartois.butinfo.sae.abyss.social.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository interface for User entities, providing CRUD operations and custom queries.
 */
public interface UserRepository extends MongoRepository<User, ObjectId> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    void deleteById(ObjectId id);

}
