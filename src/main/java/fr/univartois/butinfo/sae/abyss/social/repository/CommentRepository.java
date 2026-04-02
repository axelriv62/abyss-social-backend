package fr.univartois.butinfo.sae.abyss.social.repository;

import fr.univartois.butinfo.sae.abyss.social.model.Comment;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, ObjectId> {
    List<Comment> findByPostId(ObjectId postId);
}
