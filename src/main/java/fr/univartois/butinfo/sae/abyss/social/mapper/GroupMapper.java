package fr.univartois.butinfo.sae.abyss.social.mapper;

import fr.univartois.butinfo.sae.abyss.social.dto.GroupDTO;
import fr.univartois.butinfo.sae.abyss.social.model.Group;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

    public GroupDTO toDTO(Group group) {
        if (group == null) return null;

        GroupDTO dto = new GroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setTags(group.getTags());
        dto.setPosts(group.getPosts());
        dto.setCreatedAt(group.getCreatedAt());
        return dto;
    }

    public Group toEntity(GroupDTO dto) {
        if (dto == null) return null;

        Group group = new Group();
        group.setId(dto.getId());
        group.setName(dto.getName());
        group.setTags(dto.getTags());
        group.setPosts(dto.getPosts());
        group.setCreatedAt(dto.getCreatedAt());
        return group;
    }
}