package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.mapper.GroupMapper;
import fr.univartois.butinfo.sae.abyss.social.model.Group;
import fr.univartois.butinfo.sae.abyss.social.repository.GroupRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GroupService {

    // Repository used to persist Group entities in MongoDB
    private final GroupRepository groupRepository;

    /*
     // Constructor injection of required dependencies.
     // Keep constructor minimal so Spring can autowire only what is needed.
    */
    public GroupService(GroupRepository groupRepository, GroupMapper groupMapper) {
        this.groupRepository = groupRepository;
    }

    public Group save(Group group) {
        if (group.getCreatedAt() == null) {
            group.setCreatedAt(LocalDateTime.now());
        }
        return groupRepository.save(group);
    }

}
