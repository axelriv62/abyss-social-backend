package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.PageDTO;
import fr.univartois.butinfo.sae.abyss.social.dto.PostDTO;
import fr.univartois.butinfo.sae.abyss.social.mapper.PageMapper;
import fr.univartois.butinfo.sae.abyss.social.model.Page;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.service.PageService;
import fr.univartois.butinfo.sae.abyss.social.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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
    private static final String PAGE_NOT_FOUND = "Page not found";

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
     * @param currentUser The currently authenticated user, injected by Spring Security. If the user is not authenticated, this parameter will be null.
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
     * @param currentUser The currently authenticated user, injected by Spring Security. If the user is not authenticated, this parameter will be null. Only the creator of the page can perform this action. If the authenticated user is not the creator of the page, a 403 Forbidden response is returned.
     * @return A ResponseEntity with HTTP status 204 (No Content) if the deletion was successful, or 404 (Not Found) if the Page does not exist.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a page", description = "Deletes the page with the specified ID. Only the creator of the page can perform this action.")
    @ApiResponse(responseCode = "204", description = "Page successfully deleted")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Page not found")
    public ResponseEntity<Void> deletePage(@PathVariable ObjectId id, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Page page = pageService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PAGE_NOT_FOUND));

        if (!page.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        pageService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Handles the update of an existing Page by its ID.
     * @param id The ObjectId of the Page to be updated.
     * @param pageDTO The PageDTO containing the updated details of the page. The request body must contain a valid PageDTO object. If the page does not exist, a 404 Not Found response is returned.
     * @param currentUser The currently authenticated user, injected by Spring Security. If the user is not authenticated, this parameter will be null. Only the creator of the page can perform this action. If the authenticated user is not the creator of the page, a 403 Forbidden response is returned.
     * @return A ResponseEntity containing the updated PageDTO and HTTP status 200 (OK) if the update was successful, or 404 (Not Found) if the Page does not exist.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a page", description = "Updates the page with the specified ID using the provided details. Only the creator of the page can perform this action.")
    @ApiResponse(responseCode = "200", description = "Page successfully updated")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Page not found")
    public ResponseEntity<PageDTO> updatePage(@PathVariable ObjectId id, @Valid @RequestBody PageDTO pageDTO, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Page page = pageService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PAGE_NOT_FOUND));

        if (!page.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Page updatedPage = pageService.updatePage(id, pageMapper.toEntity(pageDTO));
        return ResponseEntity.ok(pageMapper.toDTO(updatedPage));
    }

    /**
     * Handles the following of a page by the authenticated user. This method allows the user to follow a page by its ID. If the user is not authenticated, a 401 Unauthorized response is returned. If the page does not exist, a 404 Not Found response is returned. If the operation is successful, a 200 OK response is returned.
     * @param id The ObjectId of the page to be followed.
     * @param currentUser The currently authenticated user, injected by Spring Security. If the user is not authenticated, this parameter will be null.
     * @return A ResponseEntity with HTTP status 200 (OK) if the user successfully followed the page, 404 (Not Found) if the page does not exist, or 401 (Unauthorized) if the user is not authenticated.
     */
    @PatchMapping("/{id}/follow")
    @Operation(summary = "Follow a page", description = "Allows the authenticated user to follow a page by its ID.")
    @ApiResponse(responseCode = "200", description = "User successfully followed the page")
    @ApiResponse(responseCode = "404", description = "Page not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<Void> followPage(@PathVariable ObjectId id, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Page page = pageService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PAGE_NOT_FOUND));
        userService.addPageToUser(currentUser.getId(), page.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Handles the unfollowing of a page by the authenticated user. This method allows the user to unfollow a page by its ID. If the user is not authenticated, a 401 Unauthorized response is returned. If the page does not exist, a 404 Not Found response is returned. If the operation is successful, a 200 OK response is returned.
     * @param id The ObjectId of the page to be unfollowed.
     * @param currentUser The currently authenticated user, injected by Spring Security. If the user is not authenticated, this parameter will be null.
     * @return A ResponseEntity with HTTP status 200 (OK) if the user successfully unfollowed the page, 404 (Not Found) if the page does not exist, or 401 (Unauthorized) if the user is not authenticated.
     */
    @PatchMapping("/{id}/unfollow")
    @Operation(summary = "Unfollow a page", description = "Allows the authenticated user to unfollow a page by its ID.")
    @ApiResponse(responseCode = "200", description = "User successfully unfollowed the page")
    @ApiResponse(responseCode = "404", description = "Page not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<Void> unfollowPage(@PathVariable ObjectId id, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Page page = pageService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PAGE_NOT_FOUND));
        userService.removePageFromUser(currentUser.getId(), page.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Handles the retrieval of a Page by its ID. This method allows clients to retrieve the details of a page by providing its ID in the URL path. If the page exists, a 200 OK response is returned with the PageDTO in the response body. If the page does not exist, a 404 Not Found response is returned.
     * @param id The ObjectId of the page to be retrieved.
     * @return A ResponseEntity containing the PageDTO of the requested page and HTTP status 200 (OK) if the page is found, or HTTP status 404 (Not Found) if the page does not exist.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get page details", description = "Retrieve the details of a page by its ID.")
    @ApiResponse(responseCode = "200", description = "Page details retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Page not found")
    public ResponseEntity<PageDTO> getPageById(@PathVariable ObjectId id) {
        Page page = pageService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PAGE_NOT_FOUND));
        return ResponseEntity.ok(pageMapper.toDTO(page));
    }

    /**
     * Handles the search for pages by name. This method allows clients to search for pages whose name contains a specified query string, case-insensitively. The query string is provided as a request parameter named "query". If the search is successful, a 200 OK response is returned with a list of matching PageDTOs in the response body. If the query parameter is invalid (e.g., empty), a 400 Bad Request response is returned.
     * @param query The query string to search for in page names. This parameter is required and should not be empty.
     * @return A ResponseEntity containing a list of PageDTOs that match the search criteria and HTTP status 200 (OK) if the search is successful, or HTTP status 400 (Bad Request) if the query parameter is invalid.
     */
    @GetMapping("/search")
    @Operation(summary = "Search pages by name", description = "Lists pages whose name contains the provided fragment, case-insensitively.")
    @ApiResponse(responseCode = "200", description = "Search completed")
    @ApiResponse(responseCode = "400", description = "Query is invalid")
    public ResponseEntity<List<PageDTO>> searchPagesByName(@RequestParam("query") String query) {
        List<Page> matches = pageService.searchByName(query);
        return ResponseEntity.ok(pageMapper.toDTOList(matches));
    }

    @GetMapping("/{id}/posts")
    @Operation(summary = "Get all posts of a page")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Posts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
            @ApiResponse(responseCode = "404", description = "Page not found")
    })
    public List<PostDTO> getPosts(@PathVariable("id") ObjectId userId, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return List.of();
        }
        return pageService.getPagesPosts(userId);
    }
}