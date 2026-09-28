package com.backend.gapfinder.services;

import com.backend.gapfinder.exceptions.NotFoundException;
import com.backend.gapfinder.models.BuildingModel;
import com.backend.gapfinder.models.OpenTableModel;
import com.backend.gapfinder.models.UserModel;
import com.backend.gapfinder.repositories.OpenTableRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OpenTableService {

    private final OpenTableRepository openTableRepository;
    private final UserService userService;
    private final BuildingService buildingService;

    public OpenTableService(OpenTableRepository openTableRepository, UserService userService,
                             BuildingService buildingService) {
        this.openTableRepository = openTableRepository;
        this.userService = userService;
        this.buildingService = buildingService;
    }

    // Get an open table by its id
    @Transactional
    public OpenTableModel getById(Long id) {
        log.info("Inicia proceso de consultar la open table con id = {}", id);
        return openTableRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La open table con id " + id + " no existe"));
    }

    // Get all open tables
    @Transactional
    public List<OpenTableModel> getAll() {
        log.info("Inicia proceso de consultar todas las open tables");
        return openTableRepository.findAll();
    }

    // Create a new open table
    @Transactional
    public OpenTableModel create(OpenTableModel openTable) {
        log.info("Inicia proceso de creación de una open table con título = {}", openTable.getTitle());

        validateOpenTableData(openTable);

        UserModel creator = userService.getById(openTable.getCreator().getId());
        BuildingModel building = buildingService.getById(openTable.getBuilding().getId());

        openTable.setId(null);
        openTable.setCreator(creator);
        openTable.setBuilding(building);
        openTable.setCreatedAt(LocalDateTime.now());

        log.info("Termina proceso de creación de una open table con título = {}", openTable.getTitle());
        return openTableRepository.save(openTable);
    }

    // Update the data from an existing open table
    @Transactional
    public OpenTableModel update(Long id, OpenTableModel openTable) {
        log.info("Inicia proceso de actualización de la open table con id = {}", id);

        OpenTableModel existente = getById(id);
        validateOpenTableData(openTable);

        UserModel creator = userService.getById(openTable.getCreator().getId());
        BuildingModel building = buildingService.getById(openTable.getBuilding().getId());

        existente.setCreator(creator);
        existente.setBuilding(building);
        existente.setTitle(openTable.getTitle());
        existente.setDescription(openTable.getDescription());
        existente.setStartTime(openTable.getStartTime());
        existente.setEndTime(openTable.getEndTime());
        existente.setMaxParticipants(openTable.getMaxParticipants());
        existente.setStatus(openTable.getStatus());

        log.info("Termina proceso de actualización de la open table con id = {}", id);
        return openTableRepository.save(existente);
    }

    // Delete an existing open table
    @Transactional
    public void delete(Long id) {
        log.info("Inicia proceso de eliminación de la open table con id = {}", id);

        OpenTableModel existente = getById(id);
        openTableRepository.delete(existente);

        log.info("Termina proceso de eliminación de la open table con id = {}", id);
    }

    // Validate that the open table data is correct
    private void validateOpenTableData(OpenTableModel openTable) {
        if (openTable.getCreator() == null || openTable.getCreator().getId() == null) {
            throw new IllegalArgumentException("Debe indicar el usuario creador (creator)");
        }

        if (openTable.getBuilding() == null || openTable.getBuilding().getId() == null) {
            throw new IllegalArgumentException("Debe indicar el edificio (building)");
        }

        if (openTable.getTitle() == null || openTable.getTitle().isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }

        if (openTable.getStartTime() == null || openTable.getEndTime() == null) {
            throw new IllegalArgumentException("La hora de inicio y fin son obligatorias");
        }

        if (!openTable.getStartTime().isBefore(openTable.getEndTime())) {
            throw new IllegalArgumentException("La hora de inicio debe ser antes de la hora de fin");
        }

        if (openTable.getMaxParticipants() == null || openTable.getMaxParticipants() <= 0) {
            throw new IllegalArgumentException("El número máximo de participantes debe ser positivo");
        }

        if (openTable.getStatus() == null) {
            throw new IllegalArgumentException("Debe indicar el estado de la open table (status)");
        }
    }

    // Count the open tables created since the given date (used for the abandonment statistics)
    @Transactional(readOnly = true)
    public long countCreatedSince(LocalDateTime since) {
        log.info("Inicia proceso de contar las open tables creadas desde {}", since);

        long count = openTableRepository.countByCreatedAtGreaterThanEqual(since);

        log.info("Termina proceso de contar las open tables creadas desde {}", since);
        return count;
    }
}