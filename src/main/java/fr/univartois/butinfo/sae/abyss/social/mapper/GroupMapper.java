package fr.univartois.butinfo.sae.abyss.social.mapper;

import fr.univartois.butinfo.sae.abyss.social.dto.GroupDTO;
import fr.univartois.butinfo.sae.abyss.social.model.Group;
import org.mapstruct.Mapper;

import java.util.List;
import org.bson.types.ObjectId;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    GroupDTO toDTO(Group group);

    Group toEntity(GroupDTO groupDTO);

    List<GroupDTO> toDTOList(List<Group> groups);

    default ObjectId[] toObjectIdArray(String[] ids) {
        if (ids == null) return null;
        return Arrays.stream(ids)
                .filter(Objects::nonNull)
                .map(String::trim)
                .flatMap(s -> {
                    try {
                        return Stream.of(new ObjectId(s));
                    } catch (IllegalArgumentException ex) {
                        // invalid hex string -> ignore this id
                        return Stream.empty();
                    }
                })
                .toArray(ObjectId[]::new);
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