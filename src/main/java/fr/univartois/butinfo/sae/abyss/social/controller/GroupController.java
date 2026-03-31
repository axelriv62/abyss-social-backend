package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.GroupDTO;
import fr.univartois.butinfo.sae.abyss.social.mapper.GroupMapper;
import fr.univartois.butinfo.sae.abyss.social.model.Group;
import fr.univartois.butinfo.sae.abyss.social.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
  * REST controller for Group resources.
  * Uses {@link GroupService} for business logic and {@link GroupMapper} to convert between DTO and entity.
  * The controller exposes endpoints under the "/groups" path and is responsible for HTTP-level concerns
  * (request mapping, validation, response codes). Domain rules such as populating {@code createdAt}
  * are enforced in the service layer.
*/
@RestController
@RequestMapping("/groups")
public class GroupController {
    // Service that performs persistence and business rules (e.g. set createdAt if null)
    private final GroupService groupService;
    // Mapper to convert between Group and GroupDTO
    private final GroupMapper groupMapper;


    /*
     // Constructor injection is used to provide dependencies.
     // This makes the controller easier to test and keeps dependencies explicit.
    */
    public GroupController(GroupService groupService, GroupMapper groupMapper) {
        this.groupService = groupService;
        this.groupMapper = groupMapper;
    }

    /*
     // Create a new Group.
     // - Accepts a JSON payload mapped to GroupDTO.
     // - @Valid triggers Jakarta Bean Validation based on annotations in GroupDTO.
     // - The mapper converts DTO -> entity.
     // - The service fills server-side fields (createdAt) and persists the entity.
     // - Returns 201 Created with the saved Group as DTO.
     // - If validation fails, a 400 Bad Request will be returned by Spring's validation handling.
    */
    @PostMapping
    @Operation(summary = "Create a new Group", description = "Create a new Group with the provided data")
    @ApiResponse(responseCode = "200", description = "User successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid data")
    public ResponseEntity<GroupDTO> createGroup(@Valid @RequestBody GroupDTO groupDTO) {

        // Convert incoming DTO to entity for persistence
        Group group = groupMapper.toEntity(groupDTO);

        // Save the entity; GroupService ensures createdAt is set when missing
        Group savedGroup = groupService.save(group);

        // Convert saved entity back to DTO and return with 201 status
        return ResponseEntity.status(HttpStatus.CREATED).body(groupMapper.toDTO(savedGroup));
    }

}