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

    public GroupService(GroupRepository groupRepository, GroupMapper groupMapper, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public Optional<Group> findById(ObjectId id) {
        return groupRepository.findById(id);
    }

    public Group updateGroup(ObjectId id, Group body) {
        return groupRepository.findById(id).map(group -> {
            if (group.getCreatedAt() == null) {
                group.setCreatedAt(LocalDateTime.now());
            }
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
        }).orElseThrow(() -> new IllegalArgumentException("Group not found: " + id));
    }

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

    public boolean deleteById(ObjectId id) {
        if (groupRepository.existsById(id)) {
            groupRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
