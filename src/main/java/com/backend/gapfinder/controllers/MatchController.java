package com.backend.gapfinder.controllers;

import com.backend.gapfinder.dto.MatchBasicDTO;
import com.backend.gapfinder.dto.MatchCompleteDTO;
import com.backend.gapfinder.models.MatchModel;
import com.backend.gapfinder.services.MatchService;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matches")
public class MatchController {

    private final MatchService matchService;
    private final ModelMapper modelMapper;

    public MatchController(MatchService matchService, ModelMapper modelMapper) {
        this.matchService = matchService;
        this.modelMapper = modelMapper;
    }

    // Get a match by its id
    // GET /matches/{id}
    @GetMapping("/{id}")
    public MatchCompleteDTO getMatch(@PathVariable Long id) {
        MatchModel match = matchService.getById(id);
        return modelMapper.map(match, MatchCompleteDTO.class);
    }

    // Get all matches
    // GET /matches
    @GetMapping
    public List<MatchCompleteDTO> getAll() {
        List<MatchModel> matches = matchService.getAll();
        return modelMapper.map(matches, new TypeToken<List<MatchCompleteDTO>>() {}.getType());
    }

    // Create a new match
    // POST /matches
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatchBasicDTO createMatch(@RequestBody MatchBasicDTO dto) {
        MatchModel matchModel = modelMapper.map(dto, MatchModel.class);
        MatchModel created = matchService.create(matchModel);
        return modelMapper.map(created, MatchBasicDTO.class);
    }

    // Update an existing match
    // PUT /matches/{id}
    @PutMapping("/{id}")
    public MatchBasicDTO updateMatch(@PathVariable Long id, @RequestBody MatchBasicDTO dto) {
        MatchModel matchModel = modelMapper.map(dto, MatchModel.class);
        MatchModel updated = matchService.update(id, matchModel);
        return modelMapper.map(updated, MatchBasicDTO.class);
    }

    // Delete a match
    // DELETE /matches/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMatch(@PathVariable Long id) {
        matchService.delete(id);
    }
}