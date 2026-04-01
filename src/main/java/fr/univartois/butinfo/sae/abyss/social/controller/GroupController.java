package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.GroupDTO;
import fr.univartois.butinfo.sae.abyss.social.mapper.GroupMapper;
import fr.univartois.butinfo.sae.abyss.social.model.Group;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


/**
  *REST controller for Group resources.
  *Uses GroupService for business logic and GroupMapper to convert between DTO and entity.
  *The service is responsible for setting server-side fields (e.g. createdAt) before persistence.
 * Here are all the routes :
 * Create Group : /groups (POST)
 * Update Group : /groups/{id} (PUT)
 * Delete Group : /groups/{id} (DELETE)
*/
@RestController
@RequestMapping("/groups")
public class GroupController {
    // Service that performs persistence and business rules (e.g. set createdAt if null)
    private final GroupService groupService;
    // Mapper to convert between Group and GroupDTO
    private final GroupMapper groupMapper;


    /**
     * Constructor for GroupController, with dependency injection of GroupService and GroupMapper.
     * @param groupService
     * @param groupMapper
     */
    public GroupController(GroupService groupService, GroupMapper groupMapper) {
        this.groupService = groupService;
        this.groupMapper = groupMapper;
    }

    /**
     * Create a new Group, with the provided data.
     * @param groupDTO The GroupDTO containing the data for the new Group.
     * @return A ResponseEntity containing the created GroupDTO with HTTP status 201 (Created). 400 (Bad Request) if the provided data is invalid.
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

    /**
     * Handles the update of an existing Group by its ID.
     * @param id The ObjectId of the Group to be updated.
     * @param groupDTO The GroupDTO containing the updated data for the Group.
     * @return A ResponseEntity containing the updated GroupDTO if the update was successful, or a 404 (Not Found) status if the Group does not exist.
     */
    @Operation(summary = "Update a group", description = "Update the group with the specified ID. If the group does not exist, a 404 Not Found response is returned.")
    @ApiResponse(responseCode = "", description = "Group successfully updated")
    @ApiResponse(responseCode = "404", description = "Group not found")
    @PutMapping("/{id}")
    public ResponseEntity<GroupDTO> updateGroup(@PathVariable("id") ObjectId id, @Valid @RequestBody GroupDTO groupDTO) {
        return groupService.findById(id)
                .map(existingGroup -> {
                    // Convert incoming DTO
                    Group toSave = groupMapper.toEntity(groupDTO);
                    // Update the existing
                    Group updatedGroup = groupService.updateGroup(id, toSave);
                    // Return the updated group
                    return ResponseEntity.ok(groupMapper.toDTO(updatedGroup));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Handles the deletion of a Group by its ID.
     * @param id The ObjectId of the Group to be deleted.
     * @return A ResponseEntity with HTTP status 204 (No Content) if the deletion was successful, or 404 (Not Found) if the Group does not exist.
     */
    @Operation(summary = "Delete a Group", description = "Deletes the Group with the specified ID. If the Group does not exist, a 404 Not Found response is returned.")
    @ApiResponse(responseCode = "204", description = "Group successfully deleted")
    @ApiResponse(responseCode = "404", description = "Group not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable ObjectId id) {
        return groupService.findById(id)
                .map(group -> {
                    groupService.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Handles the addition of a group in a user's list of groups.
     * @param groupId The ObjectId of the group to be added.
     * @return A ResponseEntity with HTTP status 200 (OK) if the addition was successful, or 404 (Not Found) if the user or group does not exist.
     */
    @Operation(summary = "Add a group to a user", description = "Adds the specified group to the user's list of groups. If the user or group does not exist, a 404 Not Found response is returned.")
    @ApiResponse(responseCode = "200", description = "Group successfully added to user")
    @ApiResponse(responseCode = "404", description = "User or group not found")
    @PostMapping("/{groupId}/join")
    public ResponseEntity<Void> addGroupToUser(@PathVariable ObjectId groupId, @AuthenticationPrincipal User currentUser) {
        groupService.addGroupToUser(currentUser.getId(), groupId);
        return ResponseEntity.ok().build();
    }

    /**
     * Handles the removal of a group in a user's list of groups.
     * @param groupId The ObjectId of the group to be removed.
     * @return A ResponseEntity with HTTP status 200 (OK) if the removal was successful, or 404 (Not Found) if the user or group does not exist.
     */
    @Operation(summary = "Remove a group from a user", description = "Removes the specified group from the user's list of groups. If the user or group does not exist, a 404 Not Found response is returned.")
    @ApiResponse(responseCode = "200", description = "Group successfully removed from user")
    @ApiResponse(responseCode = "404", description = "User or group not found")
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> removeGroupFromUser(@PathVariable ObjectId groupId, @AuthenticationPrincipal User currentUser) {
        groupService.removeGroupFromUser(currentUser.getId(), groupId);
        return ResponseEntity.ok().build();
    }
}