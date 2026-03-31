package fr.univartois.butinfo.sae.abyss.social.mapper;

import fr.univartois.butinfo.sae.abyss.social.dto.GroupDTO;
import fr.univartois.butinfo.sae.abyss.social.model.Group;
import org.mapstruct.Mapper;

import java.util.List;

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

    /**
     * Convert a {@link GroupDTO} to a {@link Group} entity.
     *
     * @param GroupDTO the DTO to convert; may be null
     * @return an entity built from the DTO, or null if the input was null
     */
    Group toEntity(GroupDTO GroupDTO);

    /**
     * Convert a list of {@link Group} entities to a list of {@link GroupDTO}.
     *
     * @param groups the list of entities to convert; may be null or empty
     * @return a list of DTOs corresponding to the input list; may be null if input was null
     */
    List<GroupDTO> toDTOList(List<Group> groups);
}