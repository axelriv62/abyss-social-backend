package fr.univartois.butinfo.sae.abyss.social.repository;

import fr.univartois.butinfo.sae.abyss.social.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository interface for User entities, providing CRUD operations and custom queries.
 */
public interface UserRepository extends MongoRepository<User, ObjectId> {

}
