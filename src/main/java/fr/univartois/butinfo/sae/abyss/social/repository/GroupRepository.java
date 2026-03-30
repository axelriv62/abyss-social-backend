package fr.univartois.butinfo.sae.abyss.social.repository;

import fr.univartois.butinfo.sae.abyss.social.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {
}