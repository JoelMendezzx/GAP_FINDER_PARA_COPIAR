package com.backend.gapfinder.services;

import com.backend.gapfinder.dto.GapBasicDTO;
import com.backend.gapfinder.dto.responses.MatchCandidateResponseDTO;
import com.backend.gapfinder.exceptions.NotFoundException;
import com.backend.gapfinder.models.GapModel;
import com.backend.gapfinder.models.MatchModel;
import com.backend.gapfinder.repositories.GapRepository;
import com.backend.gapfinder.repositories.MatchRepository;
import com.backend.gapfinder.strategies.OverlapStrategy;
import com.backend.gapfinder.strategies.SameCareerStrategy;
import com.backend.gapfinder.strategies.SharedInterestStrategy;

import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final GapRepository gapRepository;
    private final GapService gapService;
    private final OverlapStrategy overlapStrategy;
    private final SameCareerStrategy sameCareerStrategy;
    private final SharedInterestStrategy sharedInterestStrategy;
    private final ModelMapper modelMapper;

    public MatchService(MatchRepository matchRepository, GapRepository gapRepository, GapService gapService,
                         OverlapStrategy overlapStrategy, SameCareerStrategy sameCareerStrategy,
                         SharedInterestStrategy sharedInterestStrategy, ModelMapper modelMapper) {
        this.matchRepository = matchRepository;
        this.gapRepository = gapRepository;
        this.gapService = gapService;
        this.overlapStrategy = overlapStrategy;
        this.sameCareerStrategy = sameCareerStrategy;
        this.sharedInterestStrategy = sharedInterestStrategy;
        this.modelMapper = modelMapper;
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


    // Find candidate gaps for a given gap, scored and sorted from best to worst
    @Transactional(readOnly = true)
    public List<MatchCandidateResponseDTO> findCandidates(Long gapId, boolean useSameCareer, boolean useSharedInterests) {
        log.info("Inicia proceso de búsqueda de candidatos para el gap con id = {}", gapId);

        GapModel targetGap = gapService.getById(gapId);

        // Only gaps from other users that overlap in time
        List<GapModel> candidates = gapRepository.findOverlappingGapsExcludingUser(
                targetGap.getUser().getId(), targetGap.getStartTime(), targetGap.getEndTime());

        List<MatchCandidateResponseDTO> result = candidates.stream()
                .map(candidateGap -> {
                    // Base score always applies
                    double score = overlapStrategy.calculateScore(targetGap, candidateGap);

                    // Optional bonuses only if the user selected them
                    if (useSameCareer) {
                        score += sameCareerStrategy.calculateScore(targetGap, candidateGap);
                    }
                    if (useSharedInterests) {
                        score += sharedInterestStrategy.calculateScore(targetGap, candidateGap);
                    }

                    MatchCandidateResponseDTO dto = new MatchCandidateResponseDTO();
                    dto.setProposerGap(modelMapper.map(targetGap, GapBasicDTO.class));
                    dto.setAcceptorGap(modelMapper.map(candidateGap, GapBasicDTO.class));
                    dto.setScore(score);
                    return dto;
                })
                // Discard candidates with no real overlap
                .filter(dto -> dto.getScore() > 0)
                .sorted(Comparator.comparingDouble(MatchCandidateResponseDTO::getScore).reversed())
                .toList();

        log.info("Termina proceso de búsqueda de candidatos para el gap con id = {}, encontrados = {}",
                gapId, result.size());
        return result;
    }
}