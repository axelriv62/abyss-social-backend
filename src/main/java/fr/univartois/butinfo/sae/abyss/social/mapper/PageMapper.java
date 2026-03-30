package fr.univartois.butinfo.sae.abyss.social.mapper;

import fr.univartois.butinfo.sae.abyss.social.dto.PageDTO;
import fr.univartois.butinfo.sae.abyss.social.model.Page;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PageMapper {
    PageDTO toDTO(Page page);

    Page toEntity(PageDTO pageDTO);

    List<PageDTO> toDTOList(List<Page> pages);
}
