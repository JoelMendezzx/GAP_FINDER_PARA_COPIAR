package com.backend.gapfinder.controllers;

import com.backend.gapfinder.dto.responses.OpenTableRecommendationResponseDTO;
import com.backend.gapfinder.services.RecommendationService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    // Get open tables recommended in the user's favorite building that match one of their gaps
    // GET /recommendations/open-tables/{userId}
    @GetMapping("/open-tables/{userId}")
    public OpenTableRecommendationResponseDTO recommendOpenTables(@PathVariable Long userId) {
        return recommendationService.recommendOpenTables(userId);
    }
}