package com.backend.gapfinder.dto.responses;

import com.backend.gapfinder.dto.GapBasicDTO;

import lombok.Data;

@Data
public class MatchCandidateResponseDTO {

    // Target gap owned by the requesting student
    private GapBasicDTO proposerGap;

    // Candidate gap belonging to the potential match
    private GapBasicDTO acceptorGap;

    // Total compatibility score calculated by active strategies
    private Double score;
}