package fr.univartois.butinfo.sae.abyss.social.mapper;

import fr.univartois.butinfo.sae.abyss.social.dto.CommentDTO;
import fr.univartois.butinfo.sae.abyss.social.model.Comment;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", expression = "java(ObjectIdConverter.objectIdToString(comment.getId()))")
    @Mapping(source = "user", target = "userId")
    CommentDTO toDTO(Comment comment);

    List<CommentDTO> toDTOs(List<Comment> comments);

    @Mapping(target = "id", expression = "java(ObjectIdConverter.stringToObjectId(commentDTO.id()))")
    @Mapping(target = "post", ignore = true)
    @Mapping(source = "userId", target = "user")
    Comment toEntity(@Valid CommentDTO commentDTO);

    /**
     * Maps a user to its identifier.
     *
     * @param user the source user.
     * @return the user identifier, or {@code null} if the user is {@code null}.
     */
    default String map(User user) {
        return user != null ? ObjectIdConverter.objectIdToString(user.getId()) : null;
    }

    /**
     * Maps a user identifier to a user instance.
     *
     * @param userId the source user identifier.
     * @return a user with the given identifier, or {@code null} if the identifier is {@code null}.
     */
    default User map(String userId) {
        ObjectId objectId = ObjectIdConverter.stringToObjectId(userId);
        if (objectId == null) {
            return null;
        }
        User user = new User();
        user.setId(objectId);
        return user;
    }
}

