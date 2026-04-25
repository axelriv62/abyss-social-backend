package fr.univartois.butinfo.sae.abyss.social.mapper;

import fr.univartois.butinfo.sae.abyss.social.dto.PageDTO;
import fr.univartois.butinfo.sae.abyss.social.model.Page;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Maps {@link Page} entities to {@link PageDTO} objects and vice versa.
 */
@Mapper(componentModel = "spring")
public interface PageMapper {

    @Mapping(target = "id", expression = "java(ObjectIdConverter.objectIdToString(page.getId()))")
    @Mapping(target = "userId", expression = "java(page.getUser() == null ? null : ObjectIdConverter.objectIdToString(page.getUser().getId()))")
    PageDTO toDTO(Page page);

    @Mapping(target = "id", expression = "java(ObjectIdConverter.stringToObjectId(pageDTO.id()))")
    @Mapping(target = "user", expression = "java(mapUser(pageDTO.userId()))")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    Page toEntity(PageDTO pageDTO);

    List<PageDTO> toDTOList(List<Page> pages);

    default User mapUser(String userId) {
        ObjectId objectId = ObjectIdConverter.stringToObjectId(userId);
        if (objectId == null) {
            return null;
        }
        User user = new User();
        user.setId(objectId);
        return user;
    }
}
