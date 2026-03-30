package fr.univartois.butinfo.sae.abyss.social.mapper;

import fr.univartois.butinfo.sae.abyss.social.dto.UserDTO;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", defaultValue = "USER")
    @Mapping(target = "friends", defaultExpression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "usersBanned", defaultExpression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "groups", defaultExpression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "pages", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    User toEntity(UserDTO userDTO);

    List<UserDTO> toDTOList(List<User> users);
}

