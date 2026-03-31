package fr.univartois.butinfo.sae.abyss.social.mapper;

import fr.univartois.butinfo.sae.abyss.social.dto.GroupDTO;
import fr.univartois.butinfo.sae.abyss.social.model.Group;
import org.mapstruct.Mapper;

import java.util.List;
import org.bson.types.ObjectId;
import java.util.Arrays;
import java.util.Objects;
import java.util.ArrayList;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    GroupDTO toDTO(Group group);

    Group toEntity(GroupDTO groupDTO);

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
        if (ids == null) return null;
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
