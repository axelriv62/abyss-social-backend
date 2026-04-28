package fr.univartois.butinfo.sae.abyss.social.mapper;

import fr.univartois.butinfo.sae.abyss.social.dto.PostDTO;
import fr.univartois.butinfo.sae.abyss.social.model.Post;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
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
    @Mapping(target = "id", expression = "java(ObjectIdConverter.objectIdToString(post.getId()))")
    @Mapping(target = "userId", expression = "java(ObjectIdConverter.objectIdToString(post.getUser().getId()))")
    @Mapping(target = "image", expression = "java(toPostImageDataUrl(post))")
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "dislikes", ignore = true)
    @Mapping(target = "comments", ignore = true)
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
                mapObjectIdsToStrings(Arrays.asList(post.getComments())),
                mapUsersToIds(post.getLikes()),
                mapUsersToIds(post.getDislikes()),
                partial.createdAt()
        );
    }

    /**
     * Converts a PostDTO to a Post entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "dislikes", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "user", ignore = true)
    Post toEntityPartial(@Valid PostDTO postDTO);

    /**
     * Maps a PostDTO to a Post entity with full mapping of likes and dislikes.
     */
    default Post toEntity(@Valid PostDTO postDTO) {
        if (postDTO == null) {
            return null;
        }

        Post partial = toEntityPartial(postDTO);
        partial.setLikes(mapStringsToUsers(postDTO.likes()));
        partial.setDislikes(mapStringsToUsers(postDTO.dislikes()));
        partial.setComments(mapStringsToObjectIds(postDTO.comments()).toArray(new ObjectId[0]));

        return partial;
    }

    /**
     * Converts a Post entity's image to a data URL.
     * Returns null if no image is present.
     *
     * @param post the Post entity
     * @return the image as a data URL (e.g., "data:image/png;base64,...") or null
     */
    default String toPostImageDataUrl(Post post) {
        if (post == null || post.getImage() == null || post.getImage().getData() == null) {
            return null;
        }

        String mimeType = post.getImageContentType();
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = inferMimeType(post.getImage().getData());
        }

        String base64 = Base64.getEncoder().encodeToString(post.getImage().getData());
        return "data:" + mimeType + ";base64," + base64;
    }

    /**
     * Infers the MIME type from binary data.
     *
     * @param bytes the binary data
     * @return the inferred MIME type
     */
    default String inferMimeType(byte[] bytes) {
        if (bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47) {
            return "image/png";
        }
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (bytes.length >= 6
                && bytes[0] == 'G'
                && bytes[1] == 'I'
                && bytes[2] == 'F') {
            return "image/gif";
        }
        if (bytes.length >= 12
                && bytes[0] == 'R'
                && bytes[1] == 'I'
                && bytes[2] == 'F'
                && bytes[3] == 'F'
                && bytes[8] == 'W'
                && bytes[9] == 'E'
                && bytes[10] == 'B'
                && bytes[11] == 'P') {
            return "image/webp";
        }
        return "application/octet-stream";
    }

    /**
     * Maps a user to its identifier as String.
     *
     * @param user the source user
     * @return the user identifier as String, or null if the user is null
     */
    default String map(User user) {
        return user != null ? ObjectIdConverter.objectIdToString(user.getId()) : null;
    }

    /**
     * Maps a user identifier (String) to a user instance.
     *
     * @param userId the source user identifier
     * @return a user with the given identifier, or null if the identifier is null
     */
    default User map(String userId) {
        if (userId == null) {
            return null;
        }
        User user = new User();
        user.setId(ObjectIdConverter.stringToObjectId(userId));
        return user;
    }

    /**
     * Converts a list of users to their ObjectIds as Strings.
     */
    default String[] mapUsersToIds(List<User> users) {
        if (users == null || users.isEmpty()) {
            return new String[0];
        }
        return users.stream()
                .map(this::map)
                .filter(Objects::nonNull)
                .toArray(String[]::new);
    }

    /**
     * Converts an array of Strings (user IDs) to a list of users.
     */
    default List<User> mapStringsToUsers(String[] ids) {
        List<User> users = new ArrayList<>();
        if (ids == null) {
            return users;
        }
        for (String id : ids) {
            User user = map(id);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }

    /**
     * Converts a list of ObjectIds to an array of Strings.
     */
    default String[] mapObjectIdsToStrings(List<ObjectId> ids) {
        if (ids == null || ids.isEmpty()) {
            return new String[0];
        }
        return ids.stream()
                .map(ObjectIdConverter::objectIdToString)
                .filter(Objects::nonNull)
                .toArray(String[]::new);
    }

    /**
     * Converts an array of Strings to a list of ObjectIds.
     */
    default List<ObjectId> mapStringsToObjectIds(String[] ids) {
        List<ObjectId> objectIds = new ArrayList<>();
        if (ids == null) {
            return objectIds;
        }
        for (String id : ids) {
            ObjectId objectId = ObjectIdConverter.stringToObjectId(id);
            if (objectId != null) {
                objectIds.add(objectId);
            }
        }
        return objectIds;
    }
}