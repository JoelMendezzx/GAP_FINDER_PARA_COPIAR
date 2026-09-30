package com.backend.gapfinder.controllers;

import com.backend.gapfinder.dto.GapBasicDTO;
import com.backend.gapfinder.models.GapModel;
import com.backend.gapfinder.services.GapService;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gaps")
public class GapController {

    private final GapService gapService;
    private final ModelMapper modelMapper;

    public GapController(GapService gapService, ModelMapper modelMapper) {
        this.gapService = gapService;
        this.modelMapper = modelMapper;
    }

    // Get a gap by its id
    // GET /gaps/{id}
    @GetMapping("/{id}")
    public GapBasicDTO getGap(@PathVariable Long id) {
        GapModel gap = gapService.getById(id);
        return modelMapper.map(gap, GapBasicDTO.class);
    }

    // Get all gaps
    // GET /gaps
    @GetMapping
    public List<GapBasicDTO> getAll() {
        List<GapModel> gaps = gapService.getAll();
        return modelMapper.map(gaps, new TypeToken<List<GapBasicDTO>>() {}.getType());
    }

    // Create a new gap
    // POST /gaps
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GapBasicDTO createGap(@RequestBody GapBasicDTO dto) {
        GapModel gapModel = modelMapper.map(dto, GapModel.class);
        GapModel created = gapService.create(gapModel);
        return modelMapper.map(created, GapBasicDTO.class);
    }

    // Update an existing gap
    // PUT /gaps/{id}
    @PutMapping("/{id}")
    public GapBasicDTO updateGap(@PathVariable Long id, @RequestBody GapBasicDTO dto) {
        GapModel gapModel = modelMapper.map(dto, GapModel.class);
        GapModel updated = gapService.update(id, gapModel);
        return modelMapper.map(updated, GapBasicDTO.class);
    }

    // Delete a gap
    // DELETE /gaps/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGap(@PathVariable Long id) {
        gapService.delete(id);
    }
}