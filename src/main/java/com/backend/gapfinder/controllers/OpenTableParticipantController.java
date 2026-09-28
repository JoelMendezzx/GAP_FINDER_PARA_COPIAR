package com.backend.gapfinder.controllers;

import com.backend.gapfinder.dto.OpenTableParticipantBasicDTO;
import com.backend.gapfinder.models.OpenTableParticipantModel;
import com.backend.gapfinder.services.OpenTableParticipantService;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/open-table-participants")
public class OpenTableParticipantController {

    private final OpenTableParticipantService openTableParticipantService;
    private final ModelMapper modelMapper;

    public OpenTableParticipantController(OpenTableParticipantService openTableParticipantService,
                                          ModelMapper modelMapper) {
        this.openTableParticipantService = openTableParticipantService;
        this.modelMapper = modelMapper;
    }

    // Get a participant record by its id
    // GET /open-table-participants/{id}
    @GetMapping("/{id}")
    public OpenTableParticipantBasicDTO getParticipant(@PathVariable Long id) {
        OpenTableParticipantModel participant = openTableParticipantService.getById(id);
        return modelMapper.map(participant, OpenTableParticipantBasicDTO.class);
    }

    // Get all participant records
    // GET /open-table-participants
    @GetMapping
    public List<OpenTableParticipantBasicDTO> getAll() {
        List<OpenTableParticipantModel> participants = openTableParticipantService.getAll();
        return modelMapper.map(participants, new TypeToken<List<OpenTableParticipantBasicDTO>>() {}.getType());
    }

    // Create a new participant record
    // POST /open-table-participants
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpenTableParticipantBasicDTO createParticipant(@RequestBody OpenTableParticipantBasicDTO dto) {
        OpenTableParticipantModel participantModel = modelMapper.map(dto, OpenTableParticipantModel.class);
        OpenTableParticipantModel created = openTableParticipantService.create(participantModel);
        return modelMapper.map(created, OpenTableParticipantBasicDTO.class);
    }

    // Update an existing participant record
    // PUT /open-table-participants/{id}
    @PutMapping("/{id}")
    public OpenTableParticipantBasicDTO updateParticipant(@PathVariable Long id,
                                                          @RequestBody OpenTableParticipantBasicDTO dto) {
        OpenTableParticipantModel participantModel = modelMapper.map(dto, OpenTableParticipantModel.class);
        OpenTableParticipantModel updated = openTableParticipantService.update(id, participantModel);
        return modelMapper.map(updated, OpenTableParticipantBasicDTO.class);
    }

    // Delete a participant record
    // DELETE /open-table-participants/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteParticipant(@PathVariable Long id) {
        openTableParticipantService.delete(id);
    }
}