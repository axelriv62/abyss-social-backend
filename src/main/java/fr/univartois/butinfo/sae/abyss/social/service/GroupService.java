package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.mapper.GroupMapper;
import fr.univartois.butinfo.sae.abyss.social.model.Group;
import fr.univartois.butinfo.sae.abyss.social.repository.GroupRepository;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import fr.univartois.butinfo.sae.abyss.social.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;


    /**
     * Constructor for GroupService.
     * @param groupRepository The repository used to persist Group entities.
     *
    */
    public GroupService(GroupRepository groupRepository, GroupMapper groupMapper, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    /**
     * Find a group by its id in the database and return it.
     * @param id the id of the group to find
     * @return the group
     */
    public Optional<Group> findById(ObjectId id) {
        return groupRepository.findById(id);
    }

    /**
     * Update a group by its id in the database and return the updated group.
     * @param id the id of the group to update
     * @param body the new body
     * @return the updated group
     * @throws IllegalArgumentException if group not found
     */
    public Group updateGroup(ObjectId id, Group body) {
        return groupRepository.findById(id).map(group -> {
            if (body.getName() != null) {
                group.setName(body.getName());
            }
            if (body.getTags() != null) {
                group.setTags(body.getTags());
            }
            if (body.getPosts() != null && body.getPosts().length > 0) {
                group.setPosts(body.getPosts());
            }
            return groupRepository.save(group);
        }).orElseThrow(() -> new IllegalArgumentException("Group not found: " + id));
    }




    /**
     * Saves a Group entity to the database.
     * @param group The Group entity to save.
     * @return The saved Group entity.
     */
    public Group save(Group group) {
        ObjectId userId = group.getUser() != null ? group.getUser().getId() : null;
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for userId=" + userId.toHexString());
        }
        return groupRepository.save(group);
    }

    /**
     * Deletes a Group entity by its ID.
     * @param id The ObjectId of the Group to be deleted.
     * @return true if the Group was successfully deleted, false if the Page does not exist.
     */
    public boolean deleteById(ObjectId id) {
        if (groupRepository.existsById(id)) {
            groupRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
