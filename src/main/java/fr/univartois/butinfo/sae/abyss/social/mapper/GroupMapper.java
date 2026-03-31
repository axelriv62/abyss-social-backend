package fr.univartois.butinfo.sae.abyss.social.mapper;

import fr.univartois.butinfo.sae.abyss.social.dto.GroupDTO;
import fr.univartois.butinfo.sae.abyss.social.model.Group;
import org.mapstruct.Mapper;

import java.util.List;
import org.bson.types.ObjectId;
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
    GroupDTO toDTO(Group group);

    Group toEntity(GroupDTO groupDTO);

    /**
     * Convert a list of {@link Group} entities to a list of {@link GroupDTO}.
     *
     * @param groups the list of entities to convert; may be null or empty
     * @return a list of DTOs corresponding to the input list; may be null if input was null
     */
    List<GroupDTO> toDTOList(List<Group> groups);

    default ObjectId[] toObjectIdArray(String[] ids) {
        if (ids == null) return null;
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