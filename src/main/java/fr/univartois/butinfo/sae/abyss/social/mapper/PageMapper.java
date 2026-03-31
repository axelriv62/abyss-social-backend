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

    /**
     * Converts a page entity to its DTO representation.
     *
     * @param page the source page entity.
     * @return the mapped page DTO.
     */
    @Mapping(source = "user", target = "userId")
    PageDTO toDTO(Page page);

    /**
     * Converts a page DTO to its entity representation.
     *
     * @param pageDTO the source page DTO.
     * @return the mapped page entity.
     */
    @Mapping(source = "userId", target = "user")
    Page toEntity(PageDTO pageDTO);

    /**
     * Converts a list of page entities to a list of page DTOs.
     *
     * @param pages the source page entities.
     * @return the mapped page DTOs.
     */
    List<PageDTO> toDTOList(List<Page> pages);

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
