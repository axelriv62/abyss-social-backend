package fr.univartois.butinfo.sae.abyss.social.mapper;

import fr.univartois.butinfo.sae.abyss.social.dto.PostDTO;
import fr.univartois.butinfo.sae.abyss.social.model.Post;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {

    PostDTO toDTO(Post post);

    List<PostDTO> toDTOs(List<Post> posts);

    @Mapping(target = "user", ignore = true)
    Post toEntity(@Valid PostDTO postDTO);


}
