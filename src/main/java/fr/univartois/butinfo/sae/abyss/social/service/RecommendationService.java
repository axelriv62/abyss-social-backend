package fr.univartois.butinfo.sae.abyss.social.service;

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

    public RecommendationService(
            @Value("${recommendation.api.base-url}") String baseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public List<UserRecommendedDTO> getUserRecommendations(ObjectId userId) {
        return restClient.get()
                .uri("/recommend/users/{userId}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<UserRecommendedDTO>>() {});
    }
}