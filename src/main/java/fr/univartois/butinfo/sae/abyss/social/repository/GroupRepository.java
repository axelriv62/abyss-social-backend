package fr.univartois.butinfo.sae.abyss.social.repository;

import fr.univartois.butinfo.sae.abyss.social.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.bson.types.ObjectId;

import java.util.List;

@Repository
public interface GroupRepository extends MongoRepository<Group, ObjectId> {
    /**
     * Finds a list of Groups entities by their name.
     *
     * @param nameFragment The name of the group to find.
     * @return A list of Group entities with the specified name.
     */
    List<Group> findByNameContainingIgnoreCase(String nameFragment);

    List<Group> findAllById(Iterable<ObjectId> ids);
}