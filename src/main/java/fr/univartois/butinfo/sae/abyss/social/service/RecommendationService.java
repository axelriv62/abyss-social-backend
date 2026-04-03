package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.dto.GroupRecommendedDTO;
import fr.univartois.butinfo.sae.abyss.social.dto.PageRecommendedDTO;
import fr.univartois.butinfo.sae.abyss.social.dto.UserRecommendedDTO;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class RecommendationService {

    private final RestClient restClient;

    /**
     * Constructor for RecommendationService.
     * @param baseUrl The base URL of the recommendation API, injected from application properties.
     */
    public RecommendationService(
            @Value("${recommendation.api.base-url}") String baseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Get user recommendations for a given user ID.
     * @param userId The ID of the user for whom to get recommendations.
     * @return A list of UserRecommendedDTO containing recommended users.
     */
    public List<UserRecommendedDTO> getUserRecommendations(ObjectId userId) {
        return restClient.get()
                .uri("/recommend/users/{userId}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<UserRecommendedDTO>>() {});
    }

    /**
     * Get group recommendations for a given user ID.
     * @param userId The ID of the user for whom to get group recommendations.
     * @return A list of GroupRecommendedDTO containing recommended groups.
     */
    public List<GroupRecommendedDTO> getGroupRecommendations(ObjectId userId) {
        return restClient.get()
                .uri("/recommend/groups/{userId}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<GroupRecommendedDTO>>() {});
    }

    /**
     * Get page recommendations for a given user ID.
     * @param userId The ID of the user for whom to get page recommendations.
     * @return A list of PageRecommendedDTO containing recommended pages.
     */
    public List<PageRecommendedDTO> getPageRecommendations(ObjectId userId) {
        return restClient.get()
                .uri("/recommend/pages/{userId}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PageRecommendedDTO>>() {});
    }
}