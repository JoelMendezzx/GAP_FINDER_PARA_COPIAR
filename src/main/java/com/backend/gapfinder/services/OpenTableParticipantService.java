package com.backend.gapfinder.services;

import com.backend.gapfinder.exceptions.NotFoundException;
import com.backend.gapfinder.models.OpenTableModel;
import com.backend.gapfinder.models.OpenTableParticipantModel;
import com.backend.gapfinder.models.UserModel;
import com.backend.gapfinder.repositories.OpenTableParticipantRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OpenTableParticipantService {

    private final OpenTableParticipantRepository openTableParticipantRepository;
    private final UserService userService;
    private final OpenTableService openTableService;

    public OpenTableParticipantService(OpenTableParticipantRepository openTableParticipantRepository,
                                        UserService userService, OpenTableService openTableService) {
        this.openTableParticipantRepository = openTableParticipantRepository;
        this.userService = userService;
        this.openTableService = openTableService;
    }

    // Get a participant record by its id
    @Transactional
    public OpenTableParticipantModel getById(Long id) {
        log.info("Inicia proceso de consultar el participante con id = {}", id);
        return openTableParticipantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El participante con id " + id + " no existe"));
    }

    // Get all participant records
    @Transactional
    public List<OpenTableParticipantModel> getAll() {
        log.info("Inicia proceso de consultar todos los participantes");
        return openTableParticipantRepository.findAll();
    }

    // Create a new participant record
    @Transactional
    public OpenTableParticipantModel create(OpenTableParticipantModel participant) {
        log.info("Inicia proceso de creación de un registro de participante");

        validateParticipantData(participant);

        OpenTableModel openTable = openTableService.getById(participant.getOpenTable().getId());
        UserModel user = userService.getById(participant.getUser().getId());

        boolean yaExiste = openTableParticipantRepository.existsByOpenTableIdAndUserId(
                openTable.getId(), user.getId());
        if (yaExiste) {
            throw new IllegalArgumentException("El usuario ya está registrado en esta open table");
        }

        participant.setId(null);
        participant.setOpenTable(openTable);
        participant.setUser(user);
        participant.setJoinedAt(LocalDateTime.now());

        log.info("Termina proceso de creación de un registro de participante");
        return openTableParticipantRepository.save(participant);
    }

    // Update the data from an existing participant record
    @Transactional
    public OpenTableParticipantModel update(Long id, OpenTableParticipantModel participant) {
        log.info("Inicia proceso de actualización del participante con id = {}", id);

        OpenTableParticipantModel existente = getById(id);
        validateParticipantData(participant);

        OpenTableModel openTable = openTableService.getById(participant.getOpenTable().getId());
        UserModel user = userService.getById(participant.getUser().getId());

        existente.setOpenTable(openTable);
        existente.setUser(user);

        log.info("Termina proceso de actualización del participante con id = {}", id);
        return openTableParticipantRepository.save(existente);
    }

    // Delete an existing participant record
    @Transactional
    public void delete(Long id) {
        log.info("Inicia proceso de eliminación del participante con id = {}", id);

        OpenTableParticipantModel existente = getById(id);
        openTableParticipantRepository.delete(existente);

        log.info("Termina proceso de eliminación del participante con id = {}", id);
    }

    // Validate that the participant data is correct
    private void validateParticipantData(OpenTableParticipantModel participant) {
        if (participant.getOpenTable() == null || participant.getOpenTable().getId() == null) {
            throw new IllegalArgumentException("Debe indicar la open table (openTable)");
        }

        if (participant.getUser() == null || participant.getUser().getId() == null) {
            throw new IllegalArgumentException("Debe indicar el usuario (user)");
        }
    }
}