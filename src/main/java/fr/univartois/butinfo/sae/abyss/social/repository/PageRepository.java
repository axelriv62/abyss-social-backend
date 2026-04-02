package fr.univartois.butinfo.sae.abyss.social.repository;

import fr.univartois.butinfo.sae.abyss.social.model.Page;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

/**
 * The PageRepository interface provides methods for performing CRUD operations on Page entities.
 * It extends the MongoRepository interface to interact with the MongoDB database.
 */
public interface PageRepository extends MongoRepository<Page, ObjectId> {
    /**
     * Finds a list of Page entities whose names contain the specified fragment, ignoring case.
     * @param nameFragment
     * @return a list of Page entities matching the search criteria
     */
    List<Page> findByNameContainingIgnoreCase(String nameFragment);

    List<Page> findAllById(Iterable<ObjectId> ids);
}