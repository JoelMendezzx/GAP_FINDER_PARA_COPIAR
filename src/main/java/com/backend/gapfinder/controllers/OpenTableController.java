package com.backend.gapfinder.controllers;

import com.backend.gapfinder.dto.OpenTableBasicDTO;
import com.backend.gapfinder.models.OpenTableModel;
import com.backend.gapfinder.services.OpenTableService;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/open-tables")
public class OpenTableController {

    private final OpenTableService openTableService;
    private final ModelMapper modelMapper;

    public OpenTableController(OpenTableService openTableService, ModelMapper modelMapper) {
        this.openTableService = openTableService;
        this.modelMapper = modelMapper;
    }

    // Get an open table by its id
    // GET /open-tables/{id}
    @GetMapping("/{id}")
    public OpenTableBasicDTO getOpenTable(@PathVariable Long id) {
        OpenTableModel openTable = openTableService.getById(id);
        return modelMapper.map(openTable, OpenTableBasicDTO.class);
    }

    // Get all open tables
    // GET /open-tables
    @GetMapping
    public List<OpenTableBasicDTO> getAll() {
        List<OpenTableModel> openTables = openTableService.getAll();
        return modelMapper.map(openTables, new TypeToken<List<OpenTableBasicDTO>>() {}.getType());
    }

    // Count the open tables created since the given date
    // GET /open-tables/count?since=2026-09-01T00:00:00
    @GetMapping("/count")
    public long countCreatedSince(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        return openTableService.countCreatedSince(since);
    }

    // Create a new open table
    // POST /open-tables
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpenTableBasicDTO createOpenTable(@RequestBody OpenTableBasicDTO dto) {
        OpenTableModel openTableModel = modelMapper.map(dto, OpenTableModel.class);
        OpenTableModel created = openTableService.create(openTableModel);
        return modelMapper.map(created, OpenTableBasicDTO.class);
    }

    // Update an existing open table
    // PUT /open-tables/{id}
    @PutMapping("/{id}")
    public OpenTableBasicDTO updateOpenTable(@PathVariable Long id, @RequestBody OpenTableBasicDTO dto) {
        OpenTableModel openTableModel = modelMapper.map(dto, OpenTableModel.class);
        OpenTableModel updated = openTableService.update(id, openTableModel);
        return modelMapper.map(updated, OpenTableBasicDTO.class);
    }

    // Delete an open table
    // DELETE /open-tables/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOpenTable(@PathVariable Long id) {
        openTableService.delete(id);
    }
}