package fr.univartois.butinfo.sae.abyss.social.mapper;

import fr.univartois.butinfo.sae.abyss.social.dto.PostDTO;
import fr.univartois.butinfo.sae.abyss.social.model.Post;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(target = "likes", expression = "java(mapUsersToIds(post.getLikes()))")
    @Mapping(target = "dislikes", expression = "java(mapUsersToIds(post.getDislikes()))")
    @Mapping(source = "user", target = "userId")
    PostDTO toDTO(Post post);

    List<PostDTO> toDTOs(List<Post> posts);

    @Mapping(target = "likes", expression = "java(mapIdsToUsers(postDTO.likes()))")
    @Mapping(target = "dislikes", expression = "java(mapIdsToUsers(postDTO.dislikes()))")
    @Mapping(source = "userId", target = "user")
    Post toEntity(@Valid PostDTO postDTO);

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

    default ObjectId[] mapUsersToIds(List<User> users) {
        if (users == null || users.isEmpty()) {
            return new ObjectId[0];
        }
        return users.stream()
                .map(this::map)
                .filter(Objects::nonNull)
                .toArray(ObjectId[]::new);
    }

    default List<User> mapIdsToUsers(ObjectId[] ids) {
        List<User> users = new ArrayList<>();
        if (ids == null) {
            return users;
        }
        for (ObjectId id : ids) {
            User user = map(id);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }



}

