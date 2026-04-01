package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.GroupDTO;
import fr.univartois.butinfo.sae.abyss.social.dto.PageDTO;
import fr.univartois.butinfo.sae.abyss.social.mapper.GroupMapper;
import fr.univartois.butinfo.sae.abyss.social.model.Group;
import fr.univartois.butinfo.sae.abyss.social.model.Page;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.service.GroupService;
import fr.univartois.butinfo.sae.abyss.social.service.UserService;
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
import org.springframework.web.server.ResponseStatusException;


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
    private final UserService userService;


    /**
     * Constructor for GroupController, with dependency injection of GroupService and GroupMapper.
     * @param groupService
     * @param groupMapper
     */
    public GroupController(GroupService groupService, GroupMapper groupMapper, UserService userService) {
        this.groupService = groupService;
        this.groupMapper = groupMapper;
        this.userService = userService;
    }

    /**
     * Create a new Group, with the provided data.
     * @param groupDTO The GroupDTO containing the data for the new Group.
     * @param currentUser The currently authenticated user, injected by Spring Security. If the user is not authenticated, this parameter will be null.
     * @return A ResponseEntity containing the created GroupDTO with HTTP status 201 (Created). 400 (Bad Request) if the provided data is invalid.
     */
    @PostMapping
    @Operation(summary = "Create a new Group", description = "Create a new Group with the provided data")
    @ApiResponse(responseCode = "200", description = "User successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid data")
    public ResponseEntity<GroupDTO> createGroup(@Valid @RequestBody GroupDTO groupDTO, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Group group = groupMapper.toEntity(groupDTO);
        group.setUser(currentUser);
        Group savedGroup = groupService.save(group);
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
    public ResponseEntity<GroupDTO> updatePage(@PathVariable ObjectId id, @Valid @RequestBody GroupDTO groupDTO, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Group group = groupService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found"));

        if (!group.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Group updatedGroup = groupService.updateGroup(id, groupMapper.toEntity(groupDTO));
        return ResponseEntity.ok(groupMapper.toDTO(updatedGroup));
    }

    /**
     * Handles the deletion of a Group by its ID.
     * @param id The ObjectId of the Group to be deleted.
     * @return A ResponseEntity with HTTP status 204 (No Content) if the deletion was successful, or 404 (Not Found) if the Group does not exist.
     */
    @Operation(summary = "Delete a Group", description = "Deletes the Group with the specified ID. If the Group does not exist, a 404 Not Found response is returned.")
    @ApiResponse(responseCode = "204", description = "Group successfully deleted")
    @ApiResponse(responseCode = "404", description = "Group not found")
    @ApiResponse(responseCode = "403", description = "You are not the creator of this group")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable ObjectId id, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Group group = groupService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        if (!group.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        groupService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    /**
     * Handles the following of a group by the authenticated user. This method allows the user to follow a group by its ID. If the user is not authenticated, a 401 Unauthorized response is returned. If the group does not exist, a 404 Not Found response is returned. If the operation is successful, a 200 OK response is returned.
     * @param id The ObjectId of the group to be followed.
     * @param currentUser The currently authenticated user, injected by Spring Security. If the user is not authenticated, this parameter will be null.
     * @return A ResponseEntity with HTTP status 200 (OK) if the user successfully followed the group, 404 (Not Found) if the group does not exist, or 401 (Unauthorized) if the user is not authenticated.
     */
    @PatchMapping("/{id}/follow")
    @Operation(summary = "Unfollow a group", description = "Allows the authenticated user to follow a group by its ID.")
    @ApiResponse(responseCode = "200", description = "User successfully followed the group")
    @ApiResponse(responseCode = "404", description = "Group not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<Void> followGroup(@PathVariable ObjectId id, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Group page = groupService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found"));
        userService.addGroupToUser(currentUser.getId(), page.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Handles the unfollowing of a group by the authenticated user. This method allows the user to unfollow a group by its ID. If the user is not authenticated, a 401 Unauthorized response is returned. If the group does not exist, a 404 Not Found response is returned. If the operation is successful, a 200 OK response is returned.
     * @param id The ObjectId of the group to be unfollowed.
     * @param currentUser The currently authenticated user, injected by Spring Security. If the user is not authenticated, this parameter will be null.
     * @return A ResponseEntity with HTTP status 200 (OK) if the user successfully unfollowed the group, 404 (Not Found) if the group does not exist, or 401 (Unauthorized) if the user is not authenticated.
     */
    @PatchMapping("/{id}/unfollow")
    @Operation(summary = "Unfollow a group", description = "Allows the authenticated user to unfollow a group by its ID.")
    @ApiResponse(responseCode = "200", description = "User successfully unfollowed the group")
    @ApiResponse(responseCode = "404", description = "Group not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<Void> unfollowGroup(@PathVariable ObjectId id, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Group page = groupService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
        userService.removeGroupFromUser(currentUser.getId(), page.getId());
        return ResponseEntity.ok().build();
    }

}