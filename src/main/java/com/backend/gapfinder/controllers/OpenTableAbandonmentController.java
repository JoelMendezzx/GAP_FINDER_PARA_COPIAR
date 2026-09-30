package com.backend.gapfinder.controllers;

import com.backend.gapfinder.dto.OpenTableAbandonmentBasicDTO;
import com.backend.gapfinder.models.OpenTableAbandonmentModel;
import com.backend.gapfinder.services.OpenTableAbandonmentService;

import jakarta.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/open-table-abandonments")
public class OpenTableAbandonmentController {

    private final OpenTableAbandonmentService abandonmentService;
    private final ModelMapper modelMapper;

    public OpenTableAbandonmentController(OpenTableAbandonmentService abandonmentService, ModelMapper modelMapper) {
        this.abandonmentService = abandonmentService;
        this.modelMapper = modelMapper;
    }

    // Register an abandoned open table creation flow for a user
    // POST /open-table-abandonments/user/{userId}
    @PostMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public OpenTableAbandonmentBasicDTO createAbandonment(
            @PathVariable Long userId,
            @Valid @RequestBody OpenTableAbandonmentBasicDTO dto) {

        OpenTableAbandonmentModel abandonment = new OpenTableAbandonmentModel();
        abandonment.setStep(dto.step());
        abandonment.setActivityId(dto.activityId());
        abandonment.setDurationMinutes(dto.durationMinutes());
        abandonment.setMaxParticipants(dto.maxParticipants());
        abandonment.setBuildingId(dto.buildingId());

        OpenTableAbandonmentModel created = abandonmentService.create(userId, abandonment);

        return new OpenTableAbandonmentBasicDTO(
                created.getStep(),
                created.getActivityId(),
                created.getDurationMinutes(),
                created.getMaxParticipants(),
                created.getBuildingId());
    }
}