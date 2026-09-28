package com.backend.gapfinder.controllers;

import com.backend.gapfinder.dto.OpenTableAbandonmentBasicDTO;
import com.backend.gapfinder.dto.OpenTableAbandonmentStatsBasicDTO;
import com.backend.gapfinder.models.OpenTableAbandonmentModel;
import com.backend.gapfinder.services.OpenTableAbandonmentService;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/open-table-abandonments")
public class OpenTableAbandonmentController {

    private final OpenTableAbandonmentService abandonmentService;

    // Dependency injection constructor
    public OpenTableAbandonmentController(OpenTableAbandonmentService abandonmentService) {
        this.abandonmentService = abandonmentService;
    }

    // Register the step of the creation wizard where the user abandoned the flow
    // POST /open-table-abandonments
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody OpenTableAbandonmentBasicDTO dto) {
        abandonmentService.create(dto.userId(), toModel(dto));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Get abandonments, users who reached each step and abandonment rate per step since the given date
    // GET /open-table-abandonments/stats?since=2026-09-01T00:00:00
    @GetMapping("/stats")
    public ResponseEntity<List<OpenTableAbandonmentStatsBasicDTO>> getStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        return ResponseEntity.ok(abandonmentService.calculateAbandonmentRateByStep(since));
    }

    // Get the step with the highest abandonment rate since the given date (204 if there are no abandonments yet)
    // GET /open-table-abandonments/stats/most-abandoned?since=2026-09-01T00:00:00
    @GetMapping("/stats/most-abandoned")
    public ResponseEntity<OpenTableAbandonmentStatsBasicDTO> getMostAbandonedStep(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        return abandonmentService.getMostAbandonedStep(since)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    // Convert the request DTO into an OpenTableAbandonmentModel (the user is resolved in the service)
    private OpenTableAbandonmentModel toModel(OpenTableAbandonmentBasicDTO dto) {
        OpenTableAbandonmentModel abandonment = new OpenTableAbandonmentModel();
        abandonment.setStep(dto.step());
        abandonment.setActivityId(dto.activityId());
        abandonment.setDurationMinutes(dto.durationMinutes());
        abandonment.setBuildingId(dto.buildingId());
        return abandonment;
    }
}