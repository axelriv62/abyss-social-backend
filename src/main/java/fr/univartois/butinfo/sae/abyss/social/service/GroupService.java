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


/**
 * Service class for managing Group entities, providing business logic for group-related operations.
 * We can : find by id, update, save, delete
 */
/**
 * Service responsible for business rules and persistence operations for {@link Group}.
 *
 * Primary responsibilities:
 * - Persist Group entities via {@link GroupRepository}.
 * - Enforce server-side rules such as setting {@code createdAt} when missing.
 *
 * This service is a thin layer: validation is expected to be handled by DTO validation
 * and mapping is performed by a {@link GroupMapper} before persistence.
 */
@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, GroupMapper groupMapper, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    /**
     * Find a group with his ID
     * @param id The ID of the group to find
     * @return The group if found, null otherwise
     */
    public Optional<Group> findById(ObjectId id) {
        return groupRepository.findById(id);
    }

    /**
     * Update a group by its ID and the updated fields.
     * @param id The ID of the group to update.
     * @param body The Group object containing the fields to update. Only non-null fields will be updated.
     * @return The updated Group object.
     */
    public Group updateGroup(ObjectId id, Group body) {
        return groupRepository.findById(id).map(group -> {
            // Update only the fields that are present in the request body
            if (body.getName() != null) {
                group.setName(body.getName());
            }
            if (body.getTags() != null) {
                group.setTags(body.getTags());
            }
            if (body.getPosts() != null) {
                group.setPosts(body.getPosts());
            }
            return groupRepository.save(group);
            // return exception if not found
        }).orElseThrow(() -> new IllegalArgumentException("Group not found: " + id));
    }

    /**
     * Save a group. This method is used for both creating a new group and updating an existing one.
     * @param group The group to save.
     * @return The saved group.
     */
    public Group save(Group group) {
        ObjectId userId = group.getUser() != null ? group.getUser().getId() : null;
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for userId=" + userId.toHexString());
        }
        if (group.getCreatedAt() == null) {
            group.setCreatedAt(LocalDateTime.now());
        }
        return groupRepository.save(group);
    }


    /**
     * Delete the group with the given ID.
     * @param id The ID of the group to delete.
     * @return true if the group was deleted, false otherwise.
     */
    public boolean deleteById(ObjectId id) {
        if (groupRepository.existsById(id)) {
            groupRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Add a ObjectId to the ObjectId list for Groups of the user with the specified ID
     *
     * @param id      The unique identifier of the user whose joining the group
     * @param groupId The unique identifier of the group to add in the list of groups
     */
    public void addGroupToUser(ObjectId id, ObjectId groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found: " + groupId);
        }

        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id);
        }
        userRepository.findById(id).map(user -> {
            if (user.getGroups().contains(groupId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User already member of group: " + groupId);
            }
            user.getGroups().add(groupId);
            userRepository.save(user);
            return  String.format("Group %s added from user %s", groupId.toHexString(), id.toHexString());
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
    }

    /**
     * Remove a ObjectId from the ObjectId list for Groups of the user with the specified ID
     *
     * @param id      The unique identifier of the user whose leaving the group
     * @param groupId The unique identifier of the group to remove from the list of groups
     */
    public void removeGroupFromUser(ObjectId id, ObjectId groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found: " + groupId);
        }

        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id);
        }

        userRepository.findById(id).map(user -> {
            if (!user.getGroups().contains(groupId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User is not a member of group: " + groupId);
            }
            user.getGroups().remove(groupId);
            userRepository.save(user);
            return  String.format("Group %s removed from user %s", groupId.toHexString(), id.toHexString());
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
    }
}
