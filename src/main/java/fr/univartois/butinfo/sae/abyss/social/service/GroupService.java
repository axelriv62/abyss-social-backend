package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.dto.GroupDTO;
import fr.univartois.butinfo.sae.abyss.social.mapper.GroupMapper;
import fr.univartois.butinfo.sae.abyss.social.model.Group;
import fr.univartois.butinfo.sae.abyss.social.repository.GroupRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;

    public GroupService(GroupRepository groupRepository, GroupMapper groupMapper) {
        this.groupRepository = groupRepository;
        this.groupMapper = groupMapper;
    }

    public List<GroupDTO> findAll() {
        return groupRepository.findAll()
                .stream()
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<GroupDTO> findById(String id) {
        return groupRepository.findById(id)
                .map(groupMapper::toDTO);
    }

    public GroupDTO create(GroupDTO dto) {
        Group group = groupMapper.toEntity(dto);
        group.setCreatedAt(LocalDateTime.now());
        Group saved = groupRepository.save(group);
        return groupMapper.toDTO(saved);
    }

    public boolean deleteById(String id) {
        if (!groupRepository.existsById(id)) return false;
        groupRepository.deleteById(id);
        return true;
    }
}