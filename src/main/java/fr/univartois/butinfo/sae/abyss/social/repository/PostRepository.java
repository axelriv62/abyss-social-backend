package fr.univartois.butinfo.sae.abyss.social.repository;

import fr.univartois.butinfo.sae.abyss.social.model.Post;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends MongoRepository<Post, ObjectId> {

    List<Post> findByContentContainingIgnoreCase(String contentFragment);

    List<Post> findByCreatedAtBetween(LocalDateTime startInclusive, LocalDateTime endInclusive);
}
