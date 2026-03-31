package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.mapper.GroupMapper;
import fr.univartois.butinfo.sae.abyss.social.model.Group;
import fr.univartois.butinfo.sae.abyss.social.model.Page;
import fr.univartois.butinfo.sae.abyss.social.repository.GroupRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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

    public Optional<Group> findById(ObjectId id) {
        return groupRepository.findById(id);
    }

    public Optional<Group> addTagToGroup(ObjectId id, String tag_to_add) {
        return groupRepository.findById(id).map(group -> {
            String[] current_tags = group.getTags();
            String[] new_tags = new String[current_tags.length + 1];
            System.arraycopy(current_tags, 0, new_tags, 0, current_tags.length);
            new_tags[current_tags.length] = tag_to_add;
            group.setTags(new_tags);
            return groupRepository.save(group);
        });
    }

    public Group save(Group group) {
        if (group.getCreatedAt() == null) {
            group.setCreatedAt(LocalDateTime.now());
        }
        return groupRepository.save(group);
    }
    /**
     * Deletes a Page entity by its ID.
     * @param id The ObjectId of the Page to be deleted.
     * @return true if the Page was successfully deleted, false if the Page does not exist.
     */
    public boolean deleteById(ObjectId id) {
        if (groupRepository.existsById(id)) {
            groupRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
