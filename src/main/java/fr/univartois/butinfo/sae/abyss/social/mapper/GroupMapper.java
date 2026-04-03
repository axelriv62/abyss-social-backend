package fr.univartois.butinfo.sae.abyss.social.mapper;

import fr.univartois.butinfo.sae.abyss.social.dto.GroupDTO;
import fr.univartois.butinfo.sae.abyss.social.model.Group;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import org.mapstruct.Mapper;

import java.util.List;
import org.bson.types.ObjectId;
import org.mapstruct.Mapping;

import java.util.Arrays;
import java.util.Objects;
import java.util.ArrayList;

/**
 * Mapper interface to convert between Group entity and GroupDTO.
 * Implemented by MapStruct at build/runtime when componentModel = "spring" is used.
 *
 * Responsibilities:
 * - Convert a domain {@link Group} to a transport {@link GroupDTO} and vice-versa.
 * - Provide a convenience method to convert lists of entities to lists of DTOs.
 *
 * Note: This interface contains only mapping signatures; MapStruct generates the implementation.
 */
@Mapper(componentModel = "spring")
public interface GroupMapper {

    /**
     * Convert a {@link Group} entity to a {@link GroupDTO}.
     *
     * @param group the entity to convert; may be null
     * @return a DTO representing the entity, or null if the input was null
     */
    @Mapping(target = "id", expression = "java(ObjectIdConverter.objectIdToString(group.getId()))")
    @Mapping(target = "userId", expression = "java(group.getUser() == null ? null : ObjectIdConverter.objectIdToString(group.getUser().getId()))")
    @Mapping(target = "posts", expression = "java(toStringArray(group.getPosts()))")
    GroupDTO toDTO(Group group);

    @Mapping(target = "id", expression = "java(ObjectIdConverter.stringToObjectId(groupDTO.id()))")
    @Mapping(target = "user", expression = "java(mapUser(groupDTO.userId()))")
    @Mapping(target = "posts", expression = "java(toObjectIdArray(groupDTO.posts()))")
    Group toEntity(GroupDTO groupDTO);

    /**
     * Convert a list of {@link Group} entities to a list of {@link GroupDTO}.
     *
     * @param groups the list of entities to convert; may be null or empty
     * @return a list of DTOs corresponding to the input list; may be null if input was null
     */
    List<GroupDTO> toDTOList(List<Group> groups);

    default User mapUser(String userId) {
        ObjectId objectId = ObjectIdConverter.stringToObjectId(userId);
        if (objectId == null) {
            return null;
        }
        User user = new User();
        user.setId(objectId);
        return user;
    }

    default ObjectId[] toObjectIdArray(String[] ids) {
        if (ids == null) return new ObjectId[0];
        List<String> invalid = new ArrayList<>();
        ObjectId[] result = Arrays.stream(ids)
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(s -> {
                    try {
                        return new ObjectId(s);
                    } catch (IllegalArgumentException ex) {
                        invalid.add(s);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(ObjectId[]::new);

        if (!invalid.isEmpty()) {
            throw new IllegalArgumentException("Invalid ObjectId hex string(s) in posts: " + String.join(", ", invalid));
        }
        return result;
    }

    default String[] toStringArray(ObjectId[] ids) {
        if (ids == null) return new String[0];
        return Arrays.stream(ids)
                .map(ObjectId::toHexString)
                .toArray(String[]::new);
    }

    default ObjectId toObjectId(String id) {
        if (id == null) return null;
        try {
            return new ObjectId(id.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    default String fromObjectId(ObjectId id) {
        if (id == null) return null;
        return id.toHexString();
    }

}