package com.backend.gapfinder.services;

import com.backend.gapfinder.exceptions.NotFoundException;
import com.backend.gapfinder.models.GapModel;
import com.backend.gapfinder.models.MatchModel;
import com.backend.gapfinder.repositories.MatchRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final GapService gapService;

    public MatchService(MatchRepository matchRepository, GapService gapService) {
        this.matchRepository = matchRepository;
        this.gapService = gapService;
    }

    // Get a match by its id
    @Transactional
    public MatchModel getById(Long id) {
        log.info("Inicia proceso de consultar el match con id = {}", id);
        return matchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El match con id " + id + " no existe"));
    }

    // Get all matches
    @Transactional
    public List<MatchModel> getAll() {
        log.info("Inicia proceso de consultar todos los matches");
        return matchRepository.findAll();
    }

    // Create a new match
    @Transactional
    public MatchModel create(MatchModel match) {
        log.info("Inicia proceso de creación de un match");

        validateMatchData(match);

        GapModel proposerGap = gapService.getById(match.getProposerGap().getId());
        GapModel acceptorGap = gapService.getById(match.getAcceptorGap().getId());

        match.setId(null);
        match.setProposerGap(proposerGap);
        match.setAcceptorGap(acceptorGap);

        log.info("Termina proceso de creación de un match");
        return matchRepository.save(match);
    }

    // Update the data from an existing match
    @Transactional
    public MatchModel update(Long id, MatchModel match) {
        log.info("Inicia proceso de actualización del match con id = {}", id);

        MatchModel existente = getById(id);
        validateMatchData(match);

        GapModel proposerGap = gapService.getById(match.getProposerGap().getId());
        GapModel acceptorGap = gapService.getById(match.getAcceptorGap().getId());

        existente.setProposerGap(proposerGap);
        existente.setAcceptorGap(acceptorGap);
        existente.setStartTime(match.getStartTime());
        existente.setEndTime(match.getEndTime());
        existente.setScore(match.getScore());
        existente.setStatus(match.getStatus());

        log.info("Termina proceso de actualización del match con id = {}", id);
        return matchRepository.save(existente);
    }

    // Delete an existing match
    @Transactional
    public void delete(Long id) {
        log.info("Inicia proceso de eliminación del match con id = {}", id);

        MatchModel existente = getById(id);
        matchRepository.delete(existente);

        log.info("Termina proceso de eliminación del match con id = {}", id);
    }

    // Validate that the match data is correct
    private void validateMatchData(MatchModel match) {
        if (match.getProposerGap() == null || match.getProposerGap().getId() == null) {
            throw new IllegalArgumentException("Debe indicar el gap del proponente (proposerGap)");
        }

        if (match.getAcceptorGap() == null || match.getAcceptorGap().getId() == null) {
            throw new IllegalArgumentException("Debe indicar el gap del aceptante (acceptorGap)");
        }

        if (match.getStartTime() == null || match.getEndTime() == null) {
            throw new IllegalArgumentException("La hora de inicio y fin son obligatorias");
        }

        if (!match.getStartTime().isBefore(match.getEndTime())) {
            throw new IllegalArgumentException("La hora de inicio debe ser antes de la hora de fin");
        }

        if (match.getScore() == null) {
            throw new IllegalArgumentException("El score del match es obligatorio");
        }

        if (match.getStatus() == null) {
            throw new IllegalArgumentException("Debe indicar el estado del match (status)");
        }
    }
}