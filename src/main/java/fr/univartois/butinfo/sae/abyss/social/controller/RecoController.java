package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.GroupRecommendedDTO;
import fr.univartois.butinfo.sae.abyss.social.dto.PageRecommendedDTO;
import fr.univartois.butinfo.sae.abyss.social.mapper.ObjectIdConverter;
import fr.univartois.butinfo.sae.abyss.social.mapper.UserMapper;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.service.RecommendationService;
import fr.univartois.butinfo.sae.abyss.social.dto.UserRecommendedDTO;
import fr.univartois.butinfo.sae.abyss.social.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.bson.types.ObjectId;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/recommendations")
public class RecoController {
    private final RecommendationService recommendationClientService;

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Constructor for RecoController.
     * @param recommendationClientService The service used to fetch recommendations, injected by Spring's dependency injection.
     * @param userService The service for user operations.
     * @param userMapper The mapper to convert User entities to DTOs.
     */
    public RecoController(RecommendationService recommendationClientService, UserService userService, UserMapper userMapper) {
        this.recommendationClientService = recommendationClientService;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    /**
     * Endpoint to get recommended users for the authenticated user.
     * @param currentUser The currently authenticated user, injected by Spring Security.
     * @return A list of UserRecommendedDTO containing recommended users for the authenticated user.
     */
    @Operation(summary = "Get recommended users", description = "Get a list of recommended users for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved recommended users")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping("/users")
    public List<UserRecommendedDTO> getRecommendedUsers(@AuthenticationPrincipal User currentUser) {
        // return recommendationClientService.getUserRecommendations(currentUser.getId());
        // Mock to simulate recommendations while the recommandations service is down
        List<ObjectId> currentUserFriends = currentUser.getFriends() != null ? currentUser.getFriends() : new ArrayList<>();

        return userService.getAllUsers().stream()
                .filter(user -> !user.getId().equals(currentUser.getId())) // Exclude the current user
                .filter(user -> !currentUserFriends.contains(user.getId())) // Exclude users who are already friends
                .map(user -> new UserRecommendedDTO(
                        ObjectIdConverter.objectIdToString(user.getId()),
                        0.0,  // score - default to 0
                        0,    // sharedFriends
                        0,    // sharedGroups
                        0,    // sharedPages
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole(),
                        userMapper.toProfilePictureDataUrl(user),
                        user.getCreatedAt()
                ))
                .toList();
    }

    /**
     * Endpoint to get recommended groups for the authenticated user.
     * @param currentUser The currently authenticated user, injected by Spring Security.
     * @return A list of GroupRecommendedDTO containing recommended groups for the authenticated user.
     */
    @Operation(summary = "Get recommended groups", description = "Get a list of recommended groups for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved recommended groups")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping("/groups")
    public List<GroupRecommendedDTO> getRecommendedGroups(@AuthenticationPrincipal User currentUser) {
        return recommendationClientService.getGroupRecommendations(currentUser.getId());
    }

    /**
     * Endpoint to get recommended pages for the authenticated user.
     * @param currentUser The currently authenticated user, injected by Spring Security.
     * @return A list of PageRecommendedDTO containing recommended pages for the authenticated user.
     */
    @Operation(summary = "Get recommended pages", description = "Get a list of recommended pages for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved recommended pages")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping("/pages")
    public List<PageRecommendedDTO> getRecommendedPages(@AuthenticationPrincipal User currentUser) {
        return recommendationClientService.getPageRecommendations(currentUser.getId());
    }
}