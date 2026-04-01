package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.PageDTO;
import fr.univartois.butinfo.sae.abyss.social.mapper.PageMapper;
import fr.univartois.butinfo.sae.abyss.social.model.Page;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.service.PageService;
import fr.univartois.butinfo.sae.abyss.social.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * The PageController class handles HTTP requests related to the Page entity.
 * It provides endpoints for creating and managing pages.
 */
@RestController
@RequestMapping("/pages")
public class PageController {
    private final PageService pageService;
    private final UserService userService;
    private final PageMapper pageMapper;

    /**
     * Constructs a PageController with the specified PageService and PageMapper.
     *
     * @param pageService The service layer for managing Page entities.
     * @param pageMapper  The mapper for converting between Page and PageDTO.
     */
    public PageController(PageService pageService, UserService userService, PageMapper pageMapper) {
        this.pageService = pageService;
        this.userService = userService;
        this.pageMapper = pageMapper;
    }

    /**
     * Handles the creation of a new Page.
     * Accepts a PageDTO as input, converts it to a Page entity, saves it, and returns the saved PageDTO.
     *
     * @param pageDTO The PageDTO containing the details of the page to be created.
     * @return A ResponseEntity containing the created PageDTO and HTTP status 201 (Created).
     */
    @PostMapping
    @Operation(summary = "Create a new page", description = "Creates a new page with the authenticated user as the creator.")
    @ApiResponse(responseCode = "201", description = "Page successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid data")
    public ResponseEntity<PageDTO> createPage(@Valid @RequestBody PageDTO pageDTO, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Page page = pageMapper.toEntity(pageDTO);
        page.setUser(currentUser);
        Page savedPage = pageService.save(page);
        return ResponseEntity.status(HttpStatus.CREATED).body(pageMapper.toDTO(savedPage));
    }

    /**
     * Handles the deletion of a Page by its ID.
     * @param id The ObjectId of the Page to be deleted.
     * @return A ResponseEntity with HTTP status 204 (No Content) if the deletion was successful, or 404 (Not Found) if the Page does not exist.
     */
    @Operation(summary = "Delete a page", description = "Deletes the page with the specified ID. If the page does not exist, a 404 Not Found response is returned.")
    @ApiResponse(responseCode = "204", description = "Page successfully deleted")
    @ApiResponse(responseCode = "404", description = "Page not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePage(@PathVariable ObjectId id) {
        return pageService.findById(id)
                .map(page -> {
                    pageService.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Handles the update of an existing Page by its ID.
     * @param id The ObjectId of the Page to be updated.
     * @param pageDTO The PageDTO containing the updated details of the page. The request body must contain a valid PageDTO object. If the page does not exist, a 404 Not Found response is returned.
     * @return A ResponseEntity containing the updated PageDTO and HTTP status 200 (OK) if the update was successful, or 404 (Not Found) if the Page does not exist.
     */
    @Operation(summary = "Update a page", description = "Updates the page with the specified ID using the provided details. The request body must contain a valid PageDTO object. If the page does not exist, a 404 Not Found response is returned.")
    @ApiResponse(responseCode = "200", description = "Page successfully updated")
    @ApiResponse(responseCode = "400", description = "Invalid data")
    @PutMapping("/{id}")
    public ResponseEntity<PageDTO> updatePage(@PathVariable ObjectId id, @Valid @RequestBody PageDTO pageDTO) {
        return pageService.findById(id)
                .map(existingPage -> {
                    Page page = pageMapper.toEntity(pageDTO);
                    page.setId(id);
                    Page updatedPage = pageService.updatePage(id, page);
                    return ResponseEntity.ok(pageMapper.toDTO(updatedPage));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/follow")
    @Operation(summary = "Follow a page", description = "Allows the authenticated user to follow a page by its ID.")
    @ApiResponse(responseCode = "200", description = "User successfully followed the page")
    @ApiResponse(responseCode = "404", description = "Page not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<Void> followPage(@PathVariable ObjectId id, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Page page = pageService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found"));
        userService.addPageToUser(currentUser.getId(), page.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/unfollow")
    @Operation(summary = "Unfollow a page", description = "Allows the authenticated user to unfollow a page by its ID.")
    @ApiResponse(responseCode = "200", description = "User successfully unfollowed the page")
    @ApiResponse(responseCode = "404", description = "Page not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<Void> unfollowPage(@PathVariable ObjectId id, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Page page = pageService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found"));
        userService.removePageFromUser(currentUser.getId(), page.getId());
        return ResponseEntity.ok().build();
    }
}