package fr.univartois.butinfo.sae.abyss.social.repository;

import fr.univartois.butinfo.sae.abyss.social.model.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

/**
 * The PageRepository interface provides methods for performing CRUD operations on Page entities.
 * It extends the MongoRepository interface to interact with the MongoDB database.
 */
public interface PageRepository extends MongoRepository<Page, Long> {

    /**
     * Finds a list of Page entities by their name.
     *
     * @param name The name of the pages to find.
     * @return A list of Page entities with the specified name.
     */
    List<Page> findByName(String name);
}