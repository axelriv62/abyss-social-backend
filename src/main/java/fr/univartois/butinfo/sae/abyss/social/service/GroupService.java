package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.mapper.GroupMapper;
import fr.univartois.butinfo.sae.abyss.social.model.Group;
import fr.univartois.butinfo.sae.abyss.social.repository.GroupRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    // Repository used to persist Group entities in MongoDB
    private final GroupRepository groupRepository;

    /*
     // Constructor injection of required dependencies.
     // Keep constructor minimal so Spring can autowire only what is needed.
    */
    public GroupService(GroupRepository groupRepository, GroupMapper groupMapper) {
        this.groupRepository = groupRepository;
    }

    /**
     * Save a Group entity.
     *
     * Contract:
     * - If the entity's {@code createdAt} is null, it will be set to the current server time.
     * - The method delegates actual persistence to {@link GroupRepository#save(Object)}.
     *
     * @param group the entity to save; must not be null
     * @return the saved entity as returned by the repository
     */
    public Group save(Group group) {
        if (group.getCreatedAt() == null) {
            group.setCreatedAt(LocalDateTime.now());
        }
        return groupRepository.save(group);
    }

}
