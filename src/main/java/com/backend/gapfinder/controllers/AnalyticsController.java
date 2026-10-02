package com.backend.gapfinder.controllers;

import com.backend.gapfinder.dto.responses.BuildingGapPresenceResponseDTO;
import com.backend.gapfinder.dto.responses.GapCoverageResponseDTO;
import com.backend.gapfinder.dto.responses.ConnectionCompletionResponseDTO;
import com.backend.gapfinder.services.AnalyticsService;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // ==================== BQ 5 ====================

    // Get match coverage metrics grouped by gap duration, sorted by lowest coverage
    // GET /analytics/gap-coverage/by-duration
    @GetMapping("/gap-coverage/by-duration")
    public List<GapCoverageResponseDTO> getMatchCoverageByGapDuration() {
        return analyticsService.getMatchCoverageByGapDuration();
    }

    // ================== END BQ 5 ==================

    // ==================== BQ 11 ====================

    // Get student presence and free time metrics aggregated by campus building
    // GET /analytics/buildings/gap-presence
    @GetMapping("/buildings/gap-presence")
    public List<BuildingGapPresenceResponseDTO> getBuildingsByGapPresence() {
        return analyticsService.getBuildingsByGapPresence();
    }

    // ================== END BQ 11 ==================

    // ==================== BQ 7 ====================
    // Which connection method has the higher completion rate: open tables or matches?

    // GET /analytics/connection-methods/completion
    // Returns COMPLETED / total for each method across all records, with no date parameters.
    @GetMapping("/connection-methods/completion")
    public ConnectionCompletionResponseDTO getConnectionCompletion() {
        return analyticsService.getConnectionCompletion();
    }

    // ================== END BQ 7 ==================
}