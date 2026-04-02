package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.service.RecommendationService;
import fr.univartois.butinfo.sae.abyss.social.dto.UserRecommendedDTO;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
public class RecoController {
    private final RecommendationService recommendationClientService;

    public RecoController(RecommendationService recommendationClientService) {
        this.recommendationClientService = recommendationClientService;
    }

    @GetMapping("/users/{userId}")
    public List<UserRecommendedDTO> getRecommendedUsers(@PathVariable ObjectId userId) {
        return recommendationClientService.getUserRecommendations(userId);
    }
}
