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

    /**
     * Converts a Post entity to a PostDTO.
     * Handles complex mappings for likes/dislikes and user reference.
     *
     * @param post the Post entity to convert
     * @return the corresponding PostDTO
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "dislikes", ignore = true)
    PostDTO toDTOPartial(Post post);

    /**
     * Maps a list of Post entities to PostDTOs.
     */
    default List<PostDTO> toDTOs(List<Post> posts) {
        if (posts == null) {
            return new ArrayList<>();
        }
        return posts.stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Converts a Post entity to a PostDTO with full mapping of likes and dislikes.
     */
    default PostDTO toDTO(Post post) {
        if (post == null) {
            return null;
        }

        PostDTO partial = toDTOPartial(post);

        return new PostDTO(
                partial.id(),
                partial.userId(),
                partial.content(),
                partial.image(),
                partial.comments(),
                mapUsersToIds(post.getLikes()),
                mapUsersToIds(post.getDislikes()),
                partial.createdAt()
        );
    }

    /**
     * Converts a PostDTO to a Post entity.
     */
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "dislikes", ignore = true)
    @Mapping(source = "userId", target = "user")
    Post toEntityPartial(@Valid PostDTO postDTO);

    /**
     * Maps a PostDTO to a Post entity with full mapping of likes and dislikes.
     */
    default Post toEntity(@Valid PostDTO postDTO) {
        if (postDTO == null) {
            return null;
        }

        Post partial = toEntityPartial(postDTO);
        partial.setLikes(mapIdsToUsers(postDTO.likes()));
        partial.setDislikes(mapIdsToUsers(postDTO.dislikes()));

        return partial;
    }

    /**
     * Maps a user to its identifier.
     *
     * @param user the source user
     * @return the user identifier, or null if the user is null
     */
    default ObjectId map(User user) {
        return user != null ? user.getId() : null;
    }

    /**
     * Maps a user identifier to a user instance.
     *
     * @param userId the source user identifier
     * @return a user with the given identifier, or null if the identifier is null
     */
    default User map(ObjectId userId) {
        if (userId == null) {
            return null;
        }
        User user = new User();
        user.setId(userId);
        return user;
    }

    /**
     * Converts a list of users to their ObjectIds.
     */
    default ObjectId[] mapUsersToIds(List<User> users) {
        if (users == null || users.isEmpty()) {
            return new ObjectId[0];
        }
        return users.stream()
                .map(this::map)
                .filter(Objects::nonNull)
                .toArray(ObjectId[]::new);
    }

    /**
     * Converts an array of ObjectIds to a list of users.
     */
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
