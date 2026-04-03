package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.GroupRecommendedDTO;
import fr.univartois.butinfo.sae.abyss.social.dto.PageRecommendedDTO;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.service.RecommendationService;
import fr.univartois.butinfo.sae.abyss.social.dto.UserRecommendedDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
public class RecoController {
    private final RecommendationService recommendationClientService;

    /**
     * Constructor for RecoController.
     * @param recommendationClientService The service used to fetch recommendations, injected by Spring's dependency injection.
     */
    public RecoController(RecommendationService recommendationClientService) {
        this.recommendationClientService = recommendationClientService;
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
        return recommendationClientService.getUserRecommendations(currentUser.getId());
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
