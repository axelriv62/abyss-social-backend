package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.service.RecommendationService;
import fr.univartois.butinfo.sae.abyss.social.dto.UserRecommendedDTO;
import org.bson.types.ObjectId;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
public class RecoController {
    private final RecommendationService recommendationClientService;

    public RecoController(RecommendationService recommendationClientService) {
        this.recommendationClientService = recommendationClientService;
    }

    @GetMapping("/users")
    public List<UserRecommendedDTO> getRecommendedUsers(@AuthenticationPrincipal User currentUser) {
        return recommendationClientService.getUserRecommendations(currentUser.getId());
    }
}
