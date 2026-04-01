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
    @Mapping(source = "user", target = "userId")
    CommentDTO toDTO(Comment comment);

    List<CommentDTO> toDTOs(List<Comment> comments);

    @Mapping(source = "userId", target = "user")
    Comment toEntity(@Valid CommentDTO commentDTO);

    /**
     * Maps a user to its identifier.
     *
     * @param user the source user.
     * @return the user identifier, or {@code null} if the user is {@code null}.
     */
    default ObjectId map(User user) {
        return user != null ? user.getId() : null;
    }

    /**
     * Maps a user identifier to a user instance.
     *
     * @param userId the source user identifier.
     * @return a user with the given identifier, or {@code null} if the identifier is {@code null}.
     */
    default User map(ObjectId userId) {
        if (userId == null) {
            return null;
        }
        User user = new User();
        user.setId(userId);
        return user;
    }
}
